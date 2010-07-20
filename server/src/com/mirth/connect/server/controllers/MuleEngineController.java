/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.management.InstanceNotFoundException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.mule.MuleManager;
import org.mule.components.simple.PassThroughComponent;
import org.mule.impl.MuleDescriptor;
import org.mule.impl.MuleTransactionConfig;
import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.impl.endpoint.MuleEndpointURI;
import org.mule.impl.model.seda.SedaModel;
import org.mule.interceptors.InterceptorStack;
import org.mule.interceptors.LoggingInterceptor;
import org.mule.interceptors.TimerInterceptor;
import org.mule.management.agents.JmxAgent;
import org.mule.management.agents.RmiRegistryAgent;
import org.mule.routing.inbound.InboundMessageRouter;
import org.mule.routing.inbound.SelectiveConsumer;
import org.mule.routing.outbound.FilteringMulticastingRouter;
import org.mule.routing.outbound.OutboundMessageRouter;
import org.mule.umo.UMODescriptor;
import org.mule.umo.UMOInterceptor;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.manager.UMOManager;
import org.mule.umo.model.UMOModel;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.routing.UMOOutboundRouter;
import org.mule.umo.transformer.UMOTransformer;

import com.mirth.connect.connectors.jdbc.JdbcTransactionFactory;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.SystemEvent;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.model.converters.DefaultSerializerPropertiesFactory;
import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.builders.JavaScriptBuilder;
import com.mirth.connect.server.mule.ExceptionStrategy;
import com.mirth.connect.server.mule.adaptors.AdaptorFactory;
import com.mirth.connect.server.mule.filters.ValidMessageFilter;
import com.mirth.connect.server.util.GlobalChannelVariableStoreFactory;
import com.mirth.connect.server.util.GlobalVariableStore;
import com.mirth.connect.server.util.UUIDGenerator;
import com.mirth.connect.server.util.VMRegistry;
import com.mirth.connect.util.PropertyLoader;

import edu.emory.mathcs.backport.java.util.Arrays;

public class MuleEngineController implements EngineController {
    private Logger logger = Logger.getLogger(this.getClass());
    private Map<String, ConnectorMetaData> transports = null;
    private JavaScriptBuilder scriptBuilder = new JavaScriptBuilder();

    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
    private ScriptController scriptController = ControllerFactory.getFactory().createScriptController();
    private TemplateController templateController = ControllerFactory.getFactory().createTemplateController();
    private EventController eventController = ControllerFactory.getFactory().createEventController();

    private ObjectXMLSerializer objectSerializer = new ObjectXMLSerializer();
    private UMOManager muleManager = MuleManager.getInstance();
    private JmxAgent jmxAgent = null;
    private static List<String> nonConnectorProperties = null;
    private static List<String> keysOfValuesThatAreBeans = null;
    private static Map<String, String> defaultTransformers = null;

    // singleton pattern
    private static MuleEngineController instance = null;

    private MuleEngineController() {

    }

    public static MuleEngineController create() {
        synchronized (MuleEngineController.class) {
            if (instance == null) {
                instance = new MuleEngineController();
                instance.initialize();
            }

            return instance;
        }
    }

    private void initialize() {
        // list of all properties which should not be appended to the
        // connector
        nonConnectorProperties = Arrays.asList(new String[] { "host", "port", "DataType" });
        keysOfValuesThatAreBeans = Arrays.asList(new String[] { "connectionFactoryProperties", "requestVariables", "headerVariables", "envelopeProperties", "attachmentNames", "attachmentContents", "attachmentTypes", "traits", "dispatcherAttachmentNames", "dispatcherAttachmentContents", "dispatcherAttachmentTypes", "requestParamsName", "requestParamsKey", "requestParamsValue", "assertionParamsKey", "assertionParamsValue", "receiverUsernames", "receiverPasswords", "dispatcherHeaders", "dispatcherParameters" });

        // add default transformers
        defaultTransformers = new HashMap<String, String>();
        defaultTransformers.put("ByteArrayToString", "org.mule.transformers.simple.ByteArrayToString");
        defaultTransformers.put("JMSMessageToObject", "com.mirth.connect.connectors.jms.transformers.JMSMessageToObject");
        defaultTransformers.put("StringToByteArray", "org.mule.transformers.simple.StringToByteArray");
        defaultTransformers.put("ResultMapToXML", "com.mirth.connect.server.mule.transformers.ResultMapToXML");
        defaultTransformers.put("ObjectToString", "org.mule.transformers.simple.ObjectToString");
        defaultTransformers.put("NoActionTransformer", "org.mule.transformers.NoActionTransformer");
        defaultTransformers.put("HttpStringToXML", "com.mirth.connect.server.mule.transformers.HttpStringToXML");
        defaultTransformers.put("HttpRequestToString", "com.mirth.connect.server.mule.transformers.HttpRequestToString");
    }

    public void startEngine() throws ControllerException {
        logger.debug("starting mule engine");

        try {
            // remove all scripts and templates since the channels
            // were never undeployed
            scriptController.removeAllExceptGlobalScripts();
            templateController.removeAllTemplates();

            // loads the connector transport data
            transports = extensionController.getConnectorMetaData();
            resetEngine();
            muleManager.start();
            redeployAllChannels();
        } catch (Exception e) {
            logger.error("Error starting engine.", e);
        }
    }

    public void stopEngine() throws ControllerException {
        undeployChannels(getDeployedChannelIds());

        if (muleManager != null) {
            try {
                if (muleManager.isStarted()) {
                    logger.error("stopping mule engine");
                    muleManager.stop();
                }
            } catch (Exception e) {
                throw new ControllerException(e);
            } finally {
                logger.debug("disposing mule instance");
                muleManager.dispose();
            }
        }
    }

    public void deployChannels(List<Channel> channels) throws ControllerException {
        if (channels == null) {
            throw new ControllerException("Invalid channel list.");
        }

        try {
            channelController.loadCache();
            extensionController.triggerDeploy();
            scriptController.compileScripts(channels);

            List<String> registeredChannelIds = new ArrayList<String>();

            for (Channel channel : channels) {
                if (isChannelRegistered(channel.getId())) {
                    registeredChannelIds.add(channel.getId());
                }
            }

            undeployChannels(registeredChannelIds);

            scriptController.executeGlobalDeployScript();

            // update the manager with the new classes
            List<String> failedChannelIds = new ArrayList<String>();

            for (Channel channel : channels) {
                if (channel.isEnabled()) {
                    try {
                        if (!registerChannel(channel)) {
                            failedChannelIds.add(channel.getId());
                        } else {
                            clearGlobalChannelMap(channel);
                            scriptController.executeChannelDeployScript(channel.getId());
                        }
                    } catch (Exception e) {
                        logger.error("Error registering channel.", e);
                        failedChannelIds.add(channel.getId());
                    }
                }
            }

            /*
             * XXX: Unregister the failed channels if the engine is started.
             * Otherwise, the list of failed channel ids is returned to the
             * Mirth#startEngine method so that the channels can be unregistered
             * once the engine has started.
             */
            for (String channelId : failedChannelIds) {
                try {
                    unregisterChannel(channelId);
                } catch (Exception ue) {
                    logger.error("Error unregistering channel after failed deploy.", ue);
                }
            }

            channelController.loadCache();
            eventController.logSystemEvent(new SystemEvent("Channels deployed."));
        } catch (Exception e) {
            logger.error("Error deploying channels.", e);

            // if deploy fails, log to system events
            SystemEvent event = new SystemEvent("Error deploying channels.");
            event.setLevel(SystemEvent.Level.HIGH);
            event.setDescription(ExceptionUtils.getStackTrace(e));
            eventController.logSystemEvent(event);
        }
    }

    public void undeployChannels(List<String> channelIds) throws ControllerException {
        List<String> registeredChannelIds = new ArrayList<String>();
        
        // Only allow undeployment of channels that are currently deployed.
        for (String channelId : channelIds) {
            try {
                if (isChannelRegistered(channelId)) {
                    registeredChannelIds.add(channelId);
                } else {
                    logger.warn("You cannot undeploy a channel that is not currently deployed.");
                }
            } catch (Exception e) {
                logger.error("Error checking if channel is registered before undeploy.", e);
            }
        }
        
        if (registeredChannelIds.isEmpty()) {
            return;
        }

        try {
            channelController.loadCache();
            scriptController.executeGlobalShutdownScript();

            for (String registeredChannelId : registeredChannelIds) {
                channelController.getChannelCache().remove(channelController.getChannelCache().get(registeredChannelId));
                unregisterChannel(registeredChannelId);
                scriptController.executeChannelShutdownScript(registeredChannelId);
            }
        } catch (Exception e) {
            logger.error("Error undeploying channels.", e);
        }

        eventController.logSystemEvent(new SystemEvent("Channels undeployed."));
    }

    public void redeployAllChannels() throws ControllerException {
        clearGlobalMap();

        try {
            undeployChannels(getDeployedChannelIds());
            deployChannels(channelController.getChannel(null));
        } catch (Exception e) {
            logger.error("Error redeploying channels.", e);
        }
    }

    /*
     * Internal Mule logic
     */

    private boolean registerChannel(Channel channel) throws Exception {
        logger.debug("registering descriptor for channel: " + channel.getId());

        boolean registrationSuccessful = true;
        UMODescriptor descriptor = new MuleDescriptor();
        descriptor.setImplementation(Class.forName("com.mirth.connect.server.mule.components.Channel").newInstance());
        descriptor.setName(channel.getId());

        // default initial state is stopped if no state is found
        String initialState = MuleDescriptor.INITIAL_STATE_STOPPED;

        if (channel.getProperties().getProperty("initialState") != null) {
            initialState = channel.getProperties().getProperty("initialState");
        }

        descriptor.setInitialState(initialState);
        descriptor.setExceptionListener(new ExceptionStrategy());

        /*
         * If any of the endpoints/connectors fail to register, we want to
         * continue so that the descriptor is still registered.
         */
        try {
            configureInboundRouter(descriptor, channel);
        } catch (Exception e) {
            logger.error("Failed to configure inbound router.", e);
            registrationSuccessful = false;
        }

        try {
            configureOutboundRouter(descriptor, channel);
        } catch (Exception e) {
            logger.error("Failed to configure outbound router.", e);
            registrationSuccessful = false;
        }

        muleManager.getModel().registerComponent(descriptor);

        // register its mbean
        jmxAgent.registerComponentService(descriptor.getName());

        // Build up a list of all destination connectors in the channel
        List<String> endpointNames = new ArrayList<String>();
        for (int i = 1; i <= channel.getDestinationConnectors().size(); i++) {
            endpointNames.add(getConnectorNameForRouter(getConnectorReferenceForOutboundRouter(channel, i)));
        }
        // Add the source connector to the list
        endpointNames.add(getConnectorNameForRouter(getConnectorReferenceForInboundRouter(channel)));

        // Register all of the endpoint services for the given connectors
        jmxAgent.registerEndpointServices(endpointNames);

        return registrationSuccessful;
    }

    private void configureInboundRouter(UMODescriptor descriptor, Channel channel) throws Exception {
        logger.debug("configuring inbound router for channel: " + channel.getId() + " (" + channel.getName() + ")");
        InboundMessageRouter inboundRouter = new InboundMessageRouter();

        // add source endpoints
        MuleEndpoint vmEndpoint = new MuleEndpoint();
        vmEndpoint.setEndpointURI(new MuleEndpointURI(new URI("vm://" + channel.getId()).toString()));

        /*
         * XXX: Set create connector to true so that channel readers will not
         * use an existing connector (one from a different channel). Not sure if
         * this is still needed, but if this is set to 0 then the behavior of
         * mbeans being created is sometimes different during a redeploy from
         * the initial startup.
         */
        vmEndpoint.setCreateConnector(1);

        MuleEndpoint endpoint = new MuleEndpoint();
        String connectorReference = getConnectorReferenceForInboundRouter(channel);

        // Check if the channel is snychronous
        if ((channel.getProperties().get("synchronous")) != null && ((String) channel.getProperties().get("synchronous")).equalsIgnoreCase("true")) {
            endpoint.setSynchronous(true);
        }

        // STEP 1. append the default transformers required by the transport
        // (ex.
        // ByteArrayToString)
        ConnectorMetaData transport = transports.get(channel.getSourceConnector().getTransportName());
        LinkedList<UMOTransformer> transformerList = null;

        if (transport.getTransformers() != null) {
            transformerList = chainTransformers(transport.getTransformers());
        }

        // STEP 2. append the preprocessing transformer
        UMOTransformer preprocessorTransformer = registerPreprocessor(channel, connectorReference + "_preprocessor");

        if (!transformerList.isEmpty()) {
            transformerList.getLast().setTransformer(preprocessorTransformer);
        } else {
            // there were no default transformers, so make the preprocessor
            // the first transformer in the list
            transformerList.add(preprocessorTransformer);
        }

        // STEP 3. finally, append the JavaScriptTransformer that does the
        // mappings
        UMOTransformer javascriptTransformer = registerTransformer(channel, channel.getSourceConnector(), connectorReference + "_transformer");
        preprocessorTransformer.setTransformer(javascriptTransformer);

        // STEP 4. add the transformer sequence as an attribute to the endpoint
        endpoint.setTransformer(transformerList.getFirst());
        vmEndpoint.setTransformer(preprocessorTransformer);

        inboundRouter.addEndpoint(vmEndpoint);

        SelectiveConsumer selectiveConsumerRouter = new SelectiveConsumer();
        selectiveConsumerRouter.setFilter(new ValidMessageFilter());
        inboundRouter.addRouter(selectiveConsumerRouter);

        // If a channel reader is being used, add the channel id
        // to the endpointUri so the endpoint can be deployed
        String endpointUri = getEndpointUri(channel.getSourceConnector());

        if (endpointUri.equals("vm://")) {
            endpointUri += channel.getId();
        }

        endpoint.setEndpointURI(new MuleEndpointURI(endpointUri, channel.getId()));

        /*
         * MUST BE LAST STEP: Add the source connector last so that if an
         * exception occurs (like creating the URI) it wont register the JMX
         * service
         */
        UMOConnector connector = registerConnector(channel.getSourceConnector(), getConnectorNameForRouter(connectorReference), channel.getId());
        endpoint.setConnector(connector);

        inboundRouter.addEndpoint(endpoint);
        descriptor.setInboundRouter(inboundRouter);
    }

    private void configureOutboundRouter(UMODescriptor descriptor, Channel channel) throws Exception {
        logger.debug("configuring outbound router for channel: " + channel.getId() + " (" + channel.getName() + ")");
        FilteringMulticastingRouter fmr = new FilteringMulticastingRouter();
        boolean enableTransactions = false;

        for (ListIterator<Connector> iterator = channel.getDestinationConnectors().listIterator(); iterator.hasNext();) {
            Connector connector = iterator.next();

            if (connector.isEnabled()) {
                MuleEndpoint endpoint = new MuleEndpoint();
                endpoint.setEndpointURI(new MuleEndpointURI(getEndpointUri(connector), channel.getId()));

                // if there are multiple endpoints, make them all
                // synchronous to
                // ensure correct ordering of fired events
                if (channel.getDestinationConnectors().size() > 0) {
                    endpoint.setSynchronous(true);
                    // TODO: routerElement.setAttribute("synchronous",
                    // "true");
                }

                String connectorReference = getConnectorReferenceForOutboundRouter(channel, iterator.nextIndex());

                // add the destination connector
                String connectorName = getConnectorNameForRouter(connectorReference);
                endpoint.setConnector(registerConnector(connector, connectorName, channel.getId()));

                // 1. append the JavaScriptTransformer that does the
                // mappings
                UMOTransformer javascriptTransformer = registerTransformer(channel, connector, connectorReference + "_transformer");

                // 2. finally, append any transformers needed by the
                // transport (ie. StringToByteArray)
                ConnectorMetaData transport = transports.get(connector.getTransportName());
                LinkedList<UMOTransformer> defaultTransformerList = null;

                if (transport.getTransformers() != null) {
                    defaultTransformerList = chainTransformers(transport.getTransformers());

                    if (!defaultTransformerList.isEmpty()) {
                        javascriptTransformer.setTransformer(defaultTransformerList.getFirst());
                    }
                }

                // enable transactions for the outbound router only if it
                // has a JDBC connector
                if (transport.getProtocol().equalsIgnoreCase("jdbc")) {
                    enableTransactions = true;
                }

                endpoint.setTransformer(javascriptTransformer);
                fmr.addEndpoint(endpoint);
            }
        }

        // check for enabled transactions
        boolean transactional = ((channel.getProperties().get("transactional") != null) && channel.getProperties().get("transactional").toString().equalsIgnoreCase("true"));

        if (enableTransactions && transactional) {
            MuleTransactionConfig mtc = new MuleTransactionConfig();
            mtc.setActionAsString("BEGIN_OR_JOIN");
            mtc.setFactory(new JdbcTransactionFactory());
            fmr.setTransactionConfig(mtc);
        }

        OutboundMessageRouter outboundRouter = new OutboundMessageRouter();
        outboundRouter.addRouter(fmr);
        descriptor.setOutboundRouter(outboundRouter);
    }

    /**
     * Add "_connector" to the connector id
     * 
     * @param connectorId
     * @return
     */
    private String getConnectorNameForRouter(String connectorId) {
        return connectorId + "_connector";
    }

    private String getConnectorReferenceForInboundRouter(Channel channel) {
        return channel.getId() + "_source";
    }

    private String getConnectorReferenceForOutboundRouter(Channel channel, int index) {
        return channel.getId() + "_destination_" + index;
    }

    private UMOTransformer registerTransformer(Channel channel, Connector connector, String name) throws Exception {
        Transformer transformer = connector.getTransformer();
        logger.debug("registering transformer: " + name);
        UMOTransformer umoTransformer = (UMOTransformer) Class.forName("com.mirth.connect.server.mule.transformers.JavaScriptTransformer").newInstance();
        umoTransformer.setName(name);
        Map<String, Object> beanProperties = new HashMap<String, Object>();
        beanProperties.put("channelId", channel.getId());
        beanProperties.put("inboundProtocol", transformer.getInboundProtocol().toString());
        beanProperties.put("outboundProtocol", transformer.getOutboundProtocol().toString());
        beanProperties.put("encryptData", channel.getProperties().get("encryptData"));
        beanProperties.put("removeNamespace", channel.getProperties().get("removeNamespace"));
        beanProperties.put("mode", connector.getMode().toString());

        // put the outbound template in the templates table
        if (transformer.getOutboundTemplate() != null) {
            TemplateController templateController = ControllerFactory.getFactory().createTemplateController();
            IXMLSerializer<String> serializer = AdaptorFactory.getAdaptor(transformer.getOutboundProtocol()).getSerializer(transformer.getOutboundProperties());
            String templateId = UUIDGenerator.getUUID();

            if (transformer.getOutboundTemplate().length() > 0) {
                if (transformer.getOutboundProtocol().equals(MessageObject.Protocol.DICOM)) {
                    templateController.putTemplate(channel.getId(), templateId, transformer.getOutboundTemplate());
                } else {
                    templateController.putTemplate(channel.getId(), templateId, serializer.toXML(transformer.getOutboundTemplate()));
                }
            }

            beanProperties.put("templateId", templateId);
        }

        // put the script in the scripts table
        String scriptId = UUIDGenerator.getUUID();
        scriptController.putScript(channel.getId(), scriptId, scriptBuilder.getScript(channel, connector.getFilter(), transformer));
        beanProperties.put("scriptId", scriptId);
        beanProperties.put("connectorName", connector.getName());

        if (MapUtils.isNotEmpty(transformer.getInboundProperties())) {
            beanProperties.put("inboundProperties", transformer.getInboundProperties());
        }

        if (MapUtils.isNotEmpty(transformer.getOutboundProperties())) {
            beanProperties.put("outboundProperties", transformer.getOutboundProperties());
        }

        // Add the "batchScript" property to the script table
        // TODO: Make the second ID param be "batch" or something like that
        if (transformer.getInboundProperties() != null && transformer.getInboundProperties().getProperty("batchScript") != null) {
            scriptController.putScript(channel.getId(), channel.getId(), transformer.getInboundProperties().getProperty("batchScript"));
        }

        BeanUtils.populate(umoTransformer, beanProperties);
        muleManager.registerTransformer(umoTransformer);
        return umoTransformer;
    }

    private UMOConnector registerConnector(Connector connector, String name, String channelId) throws Exception {
        logger.debug("registering connector: " + name);
        // get the transport associated with this class from the transport
        // map
        ConnectorMetaData transport = transports.get(connector.getTransportName());
        UMOConnector umoConnector = (UMOConnector) Class.forName(transport.getServerClassName()).newInstance();
        umoConnector.setName(name);

        // exception-strategy
        umoConnector.setExceptionListener(new ExceptionStrategy());

        // The connector needs it's channel id (so it doesn't have to parse
        // the name) for alerts
        Map<Object, Object> beanProperties = new HashMap<Object, Object>();
        Map<Object, Object> queriesMap = new HashMap<Object, Object>();
        beanProperties.put("channelId", channelId);

        for (Entry<Object, Object> property : connector.getProperties().entrySet()) {
            if ((property.getValue() != null) && !property.getValue().equals("") && !nonConnectorProperties.contains(property.getKey())) {
                if (keysOfValuesThatAreBeans.contains(property.getKey())) {
                    beanProperties.put(property.getKey(), objectSerializer.fromXML(property.getValue().toString()));
                } else if (property.getKey().equals("script") || property.getKey().equals("ackScript")) {
                    String databaseScriptId = UUIDGenerator.getUUID();
                    scriptController.putScript(channelId, databaseScriptId, property.getValue().toString());
                    beanProperties.put(property.getKey() + "Id", databaseScriptId);
                } else if (property.getKey().equals("query") || property.getKey().equals("statement") || property.getKey().equals("ack")) {
                    queriesMap.put(property.getKey(), property.getValue());
                } else {
                    beanProperties.put(property.getKey(), property.getValue());
                }
            }
        }

        // populate the bean properties
        beanProperties.put("queries", queriesMap);

        // inboundProtocol and protocolProperties are only used in the
        // file reader connector when processing batch messages.
        beanProperties.put("inboundProtocol", connector.getTransformer().getInboundProtocol().toString());

        if (connector.getMode().equals(Connector.Mode.SOURCE)) {
            Map protocolProperties = connector.getTransformer().getInboundProperties();

            if (MapUtils.isEmpty(protocolProperties)) {
                protocolProperties = DefaultSerializerPropertiesFactory.getDefaultSerializerProperties(connector.getTransformer().getInboundProtocol());
            }

            beanProperties.put("protocolProperties", protocolProperties);
        }

        BeanUtils.populate(umoConnector, beanProperties);

        // add the connector to the manager
        muleManager.registerConnector(umoConnector);

        // register the connector service for the connector
        jmxAgent.registerConnectorService(umoConnector);

        return umoConnector;
    }

    private UMOTransformer registerPreprocessor(Channel channel, String name) throws Exception {
        UMOTransformer umoTransformer = (UMOTransformer) Class.forName("com.mirth.connect.server.mule.transformers.JavaScriptPreprocessor").newInstance();
        umoTransformer.setName(name);
        String preprocessingScriptId = UUIDGenerator.getUUID();
        scriptController.putScript(channel.getId(), preprocessingScriptId, channel.getPreprocessingScript());
        PropertyUtils.setSimpleProperty(umoTransformer, "channelId", channel.getId());
        PropertyUtils.setSimpleProperty(umoTransformer, "preprocessingScriptId", preprocessingScriptId);

        // add the transformer to the manager
        muleManager.registerTransformer(umoTransformer);
        return umoTransformer;
    }

    // Generate the endpoint URI for the specified connector.
    // The format is: protocol://host|hostname|emtpy:port
    private String getEndpointUri(Connector connector) {
        // TODO: This is a hack.
        if (connector.getProperties().getProperty("host") != null && connector.getProperties().getProperty("host").startsWith("http")) {
            return connector.getProperties().getProperty("host");
        }

        StringBuilder builder = new StringBuilder();
        builder.append(transports.get(connector.getTransportName()).getProtocol());
        builder.append("://");

        if (connector.getProperties().getProperty("host") != null) {
            builder.append(connector.getProperties().getProperty("host"));
        } else if (connector.getProperties().getProperty("hostname") != null) {
            builder.append(connector.getProperties().getProperty("hostname"));
        }

        if (connector.getProperties().getProperty("port") != null) {
            builder.append(":");
            builder.append(connector.getProperties().getProperty("port"));
        }

        return builder.toString();
    }

    private LinkedList<UMOTransformer> chainTransformers(String transformers) throws Exception {
        LinkedList<UMOTransformer> transformerList = new LinkedList<UMOTransformer>();

        if (transformers.length() != 0) {
            String[] transformerClassArray = transformers.split("\\s");
            UMOTransformer[] transformerArray = new UMOTransformer[transformerClassArray.length];

            // turn the array of class names into an array of actual Objects
            for (int i = 0; i < transformerClassArray.length; i++) {
                transformerArray[i] = (UMOTransformer) muleManager.getTransformers().get(transformerClassArray[i]);
            }

            // chain the transformers (except for the last one)
            for (int i = 0; i < transformerArray.length; i++) {
                if (i != transformerArray.length - 1) {
                    transformerArray[i].setTransformer(transformerArray[i + 1]);
                }
            }

            // turn the array of Objects into a List
            for (int i = 0; i < transformerArray.length; i++) {
                transformerList.add(transformerArray[i]);
            }
        }

        return transformerList;
    }

    private void unregisterChannel(String channelId) throws Exception {
        logger.debug("unregistering descriptor: " + channelId);
        UMODescriptor descriptor = muleManager.getModel().getDescriptor(channelId);

        try {
            muleManager.getModel().unregisterComponent(descriptor);
        } catch (Exception e) {
            logger.error("Error unregistering channel component.", e);
        }

        unregisterConnectors(descriptor.getInboundRouter().getEndpoints());
        UMOOutboundRouter outboundRouter = (UMOOutboundRouter) descriptor.getOutboundRouter().getRouters().iterator().next();
        unregisterConnectors(outboundRouter.getEndpoints());

        // Remove the associated VMMessageReceiver from the registry
        VMRegistry.getInstance().unregister(channelId);

        // remove the scripts associated with the channel
        scriptController.removeScripts(channelId);
        templateController.removeTemplates(channelId);

        // unregister its mbean
        try {
            jmxAgent.unregisterComponentService(channelId);
        } catch (InstanceNotFoundException infe) {
            logger.warn(infe);
        } catch (Exception e) {
            logger.error("Error unregistering component service: channelId=" + channelId, e);
        }
    }

    private void unregisterConnectors(List<UMOEndpoint> endpoints) throws Exception {
        for (UMOEndpoint endpoint : endpoints) {
            logger.debug("unregistering endpoint: " + endpoint.getName());

            muleManager.unregisterEndpoint(endpoint.getName());

            /*
             * Each method has a try/catch since we don't want it to abort the
             * unregistration if an exception occurs.
             */

            try {
                jmxAgent.unregisterEndpointService(endpoint.getName());
            } catch (Exception e) {
                logger.error("Error unregistering endpoint service: " + endpoint.getName(), e);
            }

            try {
                jmxAgent.unregisterConnectorService(endpoint.getConnector().getName());
            } catch (Exception e) {
                logger.error("Error unregistering connector service: " + endpoint.getConnector().getName(), e);
            }

            try {
                unregisterTransformer(endpoint.getTransformer());
            } catch (Exception e) {
                logger.error("Error unregistering transformer: " + endpoint.getTransformer(), e);
            }
        }
    }

    private void unregisterTransformer(UMOTransformer transformer) throws Exception {
        if (!defaultTransformers.keySet().contains(transformer.getName())) {
            logger.debug("unregistering transformer: " + transformer.getName());
            muleManager.unregisterTransformer(transformer.getName());
        }
    }

    private boolean isChannelRegistered(String channelId) throws Exception {
        return muleManager.getModel().isComponentRegistered(channelId);
    }

    private List<String> getDeployedChannelIds() {
        List<String> channelIds = new ArrayList<String>();

        for (Iterator<String> iterator = muleManager.getModel().getComponentNames(); iterator.hasNext();) {
            String channelId = iterator.next();

            if (!channelId.equals("MessageSink")) {
                channelIds.add(channelId);
            }
        }

        return channelIds;
    }

    private void clearGlobalMap() {
        try {
            if (configurationController.getServerProperties().getProperty("server.resetglobalvariables") == null || configurationController.getServerProperties().getProperty("server.resetglobalvariables").equals("1")) {
                logger.debug("clearing global map");
                GlobalVariableStore.getInstance().clear();
                GlobalVariableStore.getInstance().clearSync();
            }
        } catch (Exception e) {
            logger.error("Could not clear the global map.", e);
        }
    }

    private void clearGlobalChannelMap(Channel channel) {
        try {
            if (channel.getProperties().getProperty("clearGlobalChannelMap") == null || channel.getProperties().getProperty("clearGlobalChannelMap").equalsIgnoreCase("true")) {
                logger.debug("clearing global channel map for channel: " + channel.getId());
                GlobalChannelVariableStoreFactory.getInstance().get(channel.getId()).clear();
                GlobalChannelVariableStoreFactory.getInstance().get(channel.getId()).clearSync();
            }
        } catch (Exception e) {
            logger.error("Could not clear the global channel map: " + channel.getId(), e);
        }
    }

    /**
     * Resets the Mule model to a clean state.
     * 
     * @throws Exception
     */
    private void resetEngine() throws Exception {
        logger.debug("loading manager with default components");

        Properties properties = PropertyLoader.loadProperties("mirth");
        muleManager.setId("MirthConfiguration");
        MuleManager.getConfiguration().setEmbedded(true);
        MuleManager.getConfiguration().setRecoverableMode(true);
        MuleManager.getConfiguration().setClientMode(false);
        MuleManager.getConfiguration().setWorkingDirectory(ControllerFactory.getFactory().createConfigurationController().getApplicationDataDir());

        for (String transformerName : defaultTransformers.keySet()) {
            UMOTransformer umoTransformer = (UMOTransformer) Class.forName(defaultTransformers.get(transformerName)).newInstance();
            umoTransformer.setName(transformerName);
            muleManager.registerTransformer(umoTransformer);
        }

        // add interceptor stack
        InterceptorStack stack = new InterceptorStack();
        List<UMOInterceptor> interceptors = new ArrayList<UMOInterceptor>();
        interceptors.add(new LoggingInterceptor());
        interceptors.add(new TimerInterceptor());
        stack.setInterceptors(interceptors);
        muleManager.registerInterceptorStack("default", stack);

        // add model
        UMOModel model = new SedaModel();
        model.setName("Mirth");

        // add MessageSink descriptor
        UMODescriptor messageSinkDescriptor = new MuleDescriptor();
        messageSinkDescriptor.setName("MessageSink");
        messageSinkDescriptor.setImplementation(new PassThroughComponent());
        InboundMessageRouter messageSinkInboundRouter = new InboundMessageRouter();
        MuleEndpoint vmSinkEndpoint = new MuleEndpoint();
        vmSinkEndpoint.setEndpointURI(new MuleEndpointURI(new URI("vm://sink").toString()));
        messageSinkInboundRouter.addEndpoint(vmSinkEndpoint);
        messageSinkDescriptor.setInboundRouter(messageSinkInboundRouter);
        model.registerComponent(messageSinkDescriptor);

        // set the model (which also initializes and starts the model)
        muleManager.setModel(model);

        // add agents
        String port = PropertyLoader.getProperty(properties, "jmx.port");
        RmiRegistryAgent rmiRegistryAgent = new RmiRegistryAgent();
        rmiRegistryAgent.setName("RMI");
        rmiRegistryAgent.setServerUri("rmi://" + PropertyLoader.getProperty(properties, "jmx.host", "localhost") + ":" + port);
        muleManager.registerAgent(rmiRegistryAgent);

        jmxAgent = new JmxAgent();
        jmxAgent.setName("JMX");
        Map<String, String> connectorServerProperties = new HashMap<String, String>();
        connectorServerProperties.put("jmx.remote.jndi.rebind", "true");
        jmxAgent.setConnectorServerProperties(connectorServerProperties);
        jmxAgent.setConnectorServerUrl("service:jmx:rmi:///jndi/rmi://" + PropertyLoader.getProperty(properties, "jmx.host", "localhost") + ":" + port + "/server");
        logger.debug("JMX server URL: " + "service:jmx:rmi:///jndi/rmi://" + PropertyLoader.getProperty(properties, "jmx.host", "localhost") + ":" + port + "/server");
        Map<String, String> credentialsMap = new HashMap<String, String>();
        credentialsMap.put("admin", PropertyLoader.getProperty(properties, "jmx.password"));
        jmxAgent.setCredentials(credentialsMap);
        jmxAgent.setDomain(muleManager.getId());
        muleManager.registerAgent(jmxAgent);
    }
}
