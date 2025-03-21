package com.sample.core.actions.leadgeneration;

import javassist.tools.rmi.Sample.core.enums.ConfigurableQuoteStatusEnum;
import javassist.tools.rmi.Sample.core.model.ConfigurableQuoteModel;
import javassist.tools.rmi.SampleConfigurableQuoteService;
import de.hybris.platform.commerceservices.model.process.QuoteProcessModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.task.RetryLaterException;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class SendSampleConfigurableQuoteAction extends AbstractSimpleDecisionAction<QuoteProcessModel> {

    private static final Logger LOG = LoggerFactory.getLogger(SendConfigurableQuoteAction.class);

    private SampleConfigurableQuoteService configurableQuoteService;

    @Override
    public Transition executeAction(final QuoteProcessModel quoteProcessModel) throws RetryLaterException {

        final ConfigurableQuoteModel configurableQuoteModel = getConfigurableQuoteService().getConfigurableQuoteForCode(quoteProcessModel.getQuoteCode());
        if (Objects.isNull(configurableQuoteModel)) {
            LOG.error("Cannot find a Configurable Quote with code {}", quoteProcessModel.getQuoteCode());
            return Transition.NOK;
        }

        if (!ConfigurableQuoteStatusEnum.SUBMITTED.equals(configurableQuoteModel.getStatus())) {
            LOG.error("Cannot send the Configurable Quote with code {} due to its status is not submitted", quoteProcessModel.getQuoteCode());
            return Transition.NOK;
        }

        if (BooleanUtils.isFalse(getConfigurableQuoteService().submitConfigurableQuoteToPartner(configurableQuoteModel))) {
            return Transition.NOK;
        }

        return Transition.OK;
    }

    public SampleConfigurableQuoteService getConfigurableQuoteService() {
        return configurableQuoteService;
    }

    public void setConfigurableQuoteService(SampleConfigurableQuoteService configurableQuoteService) {
        this.configurableQuoteService = configurableQuoteService;
    }
}