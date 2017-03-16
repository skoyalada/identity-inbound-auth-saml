/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.saml.internal;

import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequestBuilderFactory;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponseBuilderFactory;
import org.wso2.carbon.identity.gateway.handler.response.AbstractResponseHandler;
import org.wso2.carbon.identity.gateway.handler.validator.AbstractRequestValidator;
import org.wso2.carbon.identity.gateway.service.GatewayClaimResolverService;
import org.wso2.carbon.identity.saml.request.SAML2SSORequestBuilderFactory;
import org.wso2.carbon.identity.saml.response.SAML2SSOResponseBuilderFactory;
import org.wso2.carbon.identity.saml.response.SAML2SSOResponseHandler;
import org.wso2.carbon.identity.saml.validator.IdPInitValidator;
import org.wso2.carbon.identity.saml.validator.SPInitValidator;

/**
 * SAML2 SSO Inbound Authenticator Service Component.
 */
@Component(
        name = "inbound.saml2sso.dscomponent",
        immediate = true
)
public class SAML2InboundAuthActivator implements BundleActivator {

    private Logger log = LoggerFactory.getLogger(SAML2InboundAuthActivator.class);

    @Activate
    public void start(BundleContext bundleContext) throws Exception {
        try {
            try {
                DefaultBootstrap.bootstrap();
            } catch (ConfigurationException e) {
                log.error("Error in bootstrapping the OpenSAML2 library.", e);
            }
            bundleContext.registerService(GatewayRequestBuilderFactory.class, new SAML2SSORequestBuilderFactory(), null);
            bundleContext.registerService(GatewayResponseBuilderFactory.class, new SAML2SSOResponseBuilderFactory(), null);
            bundleContext.registerService(AbstractRequestValidator.class, new SPInitValidator(), null);
            bundleContext.registerService(AbstractRequestValidator.class, new IdPInitValidator(), null);
            bundleContext.registerService(AbstractResponseHandler.class, new SAML2SSOResponseHandler(), null);
        } catch (Throwable e) {
            log.error("Error while activating SAML2 inbound authenticator component.");
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }

    @Reference(
            name = "gateway.claim.resolver",
            service = GatewayClaimResolverService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetGatewayClaimResolverService"
    )
    protected void addGatewayClaimResolverService(GatewayClaimResolverService gatewayClaimResolverService) {

        SAML2InboundAuthDataHolder.getInstance().setGatewayClaimResolverService(gatewayClaimResolverService);

        if (log.isDebugEnabled()) {
            log.debug("Binding GatewayClaimResolverService");
        }
    }

    protected void unSetGatewayClaimResolverService(GatewayClaimResolverService gatewayClaimResolverService) {

        SAML2InboundAuthDataHolder.getInstance().setGatewayClaimResolverService(null);

        if (log.isDebugEnabled()) {
            log.debug("Unbinding GatewayClaimResolverService");
        }
    }
}