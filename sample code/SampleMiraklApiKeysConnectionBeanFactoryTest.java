package com.sample.digitalplatform.mirakl.factories;

import com.mirakl.client.core.filter.internal.MiraklRequestDecorator;
import com.mirakl.client.core.internal.MiraklClientConfigWrapper;
import com.mirakl.client.mmp.front.core.MiraklMarketplacePlatformFrontApi;
import com.mirakl.hybris.core.beans.strategies.MiraklApiClientConfigurationStrategy;
import com.mirakl.hybris.core.environment.strategies.MiraklEnvironmentSelectionStrategy;
import com.mirakl.hybris.core.exceptions.MiraklApiClientException;
import com.mirakl.hybris.core.model.MiraklEnvironmentModel;
import com.sample.digitalplatform.core.config.WHRConfigService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.collections4.map.SingletonMap;
import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class SampleMiraklApiKeysConnectionBeanFactoryTest {
    private static final String MIRAKL_API_KEY_FILE = "mirakl.front.api.key.file";
    private static final String MIRAKL_ENVIRONTMENT_URL = "https://mirakl-env-url.com";
    private static final String CLIENT_API_ROLE = "apiRole";
    private ClassLoader classLoader;
    @Mock
    private WHRConfigService sampleConfigService;
    @Mock
    private MiraklApiClientConfigurationStrategy mmpFrontApiClientConfigurationStrategy;
    @Mock
    private MiraklApiClientConfigurationStrategy mmpOperatorApiClientConfigurationStrategy;
    @Mock
    private MiraklApiClientConfigurationStrategy mciFrontApiClientConfigurationStrategy;
    @Mock
    private MiraklRequestDecorator requestDecorator;
    @Mock
    private MiraklEnvironmentSelectionStrategy miraklEnvironmentSelectionStrategy;
    @Mock
    private ConfigurationService configurationService;
    @Mock
    private Configuration configuration;
    @Mock
    private MiraklEnvironmentModel miraklEnvironment;
    @InjectMocks
    private SampleMiraklApiKeysConnectionBeanFactory sut;

    @Before
    public void setUp() {
        sut.setRequestDecorators(Collections.singletonList(requestDecorator));
        classLoader = Thread.currentThread().getContextClassLoader();

        when(miraklEnvironmentSelectionStrategy.resolveCurrentMiraklEnvironment()).thenReturn(miraklEnvironment);
        when(miraklEnvironment.getApiUrl()).thenReturn(MIRAKL_ENVIRONTMENT_URL);
        when(configurationService.getConfiguration()).thenReturn(configuration);
    }

    @Test
    public void shouldReturnFrontApiClient() {
        final String apikeyFileName = classLoader.getResource("samplemirakl/test/api_key_test_txt").getPath();
        when(sampleConfigService.getStringPropertyValue(MIRAKL_API_KEY_FILE, "")).thenReturn(apikeyFileName);
        final MiraklMarketplacePlatformFrontApi mmpFrontApiClient = sut.getMmpFrontApiClient();
        assertThat(mmpFrontApiClient).isNotNull();
        assertThat(mmpFrontApiClient).isEqualTo(sut.getMmpFrontApiClient());
        verify(mmpFrontApiClientConfigurationStrategy, times(1)).configure(any(MiraklClientConfigWrapper.class));
        verify(mmpOperatorApiClientConfigurationStrategy, never()).configure(any(MiraklClientConfigWrapper.class));
        verify(mciFrontApiClientConfigurationStrategy, never()).configure(any(MiraklClientConfigWrapper.class));
    }

    @Test(expected = MiraklApiClientException.class)
    public void shoulNotFindTheFrontApiKey() {
        final String wrongApiKeyFileName = "fake/url/file";
        when(sampleConfigService.getStringPropertyValue(MIRAKL_API_KEY_FILE, "")).thenReturn(wrongApiKeyFileName);
        final MiraklMarketplacePlatformFrontApi mmpFrontApiClient = sut.getMmpFrontApiClient();
        assertThat(mmpFrontApiClient).isNull();
    }

    @Test(expected = MiraklApiClientException.class)
    public void shouldNotReturnApiKeyBecauseDoesNotExist() {
        final String emptyApikeyFileName = classLoader.getResource("samplemirakl/test/api_key_test2_txt").getPath();
        when(sampleConfigService.getStringPropertyValue(MIRAKL_API_KEY_FILE, "")).thenReturn(emptyApikeyFileName);
        sut.getMmpFrontApiClient();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldntReturnApiClientCredential() {
        final Map<String, Object> map = new SingletonMap<>(CLIENT_API_ROLE, null);
        sut.getApiClientCredential(miraklEnvironment, map);
    }
}
