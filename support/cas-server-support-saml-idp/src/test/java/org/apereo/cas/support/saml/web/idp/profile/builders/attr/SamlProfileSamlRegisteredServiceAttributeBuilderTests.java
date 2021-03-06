package org.apereo.cas.support.saml.web.idp.profile.builders.attr;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlProfileSamlRegisteredServiceAttributeBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("SAML")
public class SamlProfileSamlRegisteredServiceAttributeBuilderTests extends BaseSamlIdPConfigurationTests {

    @Autowired
    @Qualifier("samlProfileSamlAttributeStatementBuilder")
    private SamlProfileObjectBuilder<AttributeStatement> samlProfileSamlAttributeStatementBuilder;

    @Test
    public void verifyNoEncryption() {
        val service = getSamlRegisteredServiceForTestShib();
        service.setEncryptAttributes(true);

        val service2 = getSamlRegisteredServiceFor(UUID.randomUUID().toString());
        service2.setMetadataLocation("classpath:/unknown.xml");
        service2.setEncryptionDataAlgorithms(null);
        service2.setEncryptionKeyAlgorithms(null);
        service2.setEncryptAttributes(true);
        service2.setEncryptionOptional(true);

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade
            .get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();
        val statement = samlProfileSamlAttributeStatementBuilder.build(getAuthnRequestFor(service), new MockHttpServletRequest(),
            new MockHttpServletResponse(), getAssertion(), service2,
            adaptor, SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());

        assertTrue(statement.getEncryptedAttributes().isEmpty());
        assertFalse(statement.getAttributes().isEmpty());
    }

    @Test
    public void verifyEncryptionForAllUndefined() {
        val service = getSamlRegisteredServiceForTestShib();
        service.setEncryptAttributes(true);

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade
            .get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();
        val statement = samlProfileSamlAttributeStatementBuilder.build(getAuthnRequestFor(service), new MockHttpServletRequest(),
            new MockHttpServletResponse(), getAssertion(), service, adaptor, SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());

        assertFalse(statement.getEncryptedAttributes().isEmpty());
        assertTrue(statement.getAttributes().isEmpty());
    }

    @Test
    public void verifyEncryptionForAll() {
        val service = getSamlRegisteredServiceForTestShib();
        service.setEncryptAttributes(true);
        service.getEncryptableAttributes().add("*");

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();
        val statement = samlProfileSamlAttributeStatementBuilder.build(getAuthnRequestFor(service), new MockHttpServletRequest(),
            new MockHttpServletResponse(), getAssertion(), service, adaptor, SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());

        assertFalse(statement.getEncryptedAttributes().isEmpty());
        assertTrue(statement.getAttributes().isEmpty());
    }

    @Test
    public void verifyEncryptionForSome() {
        val service = getSamlRegisteredServiceForTestShib();
        service.setEncryptAttributes(true);
        service.getEncryptableAttributes().add("uid");

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();
        val statement = samlProfileSamlAttributeStatementBuilder.build(getAuthnRequestFor(service), new MockHttpServletRequest(),
            new MockHttpServletResponse(), getAssertion(), service, adaptor, SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());

        assertFalse(statement.getEncryptedAttributes().isEmpty());
        assertFalse(statement.getAttributes().isEmpty());
    }
}
