
package com.sample.digitalplatform.mirakl.dao.impl;

import com.sample.digitalplatform.mirakl.dao.MiraklMediaDao;
import com.sample.digitalplatform.mirakl.model.MarketplaceMediaModel;
import de.hybris.platform.core.GenericCondition;
import de.hybris.platform.core.GenericQuery;
import de.hybris.platform.core.GenericSearchField;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.genericsearch.GenericSearchService;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

public class DefaultSampleMiraklMediaDao extends AbstractItemDao implements MiraklMediaDao {
    private static final String ERROR_MESSAGE = "Found %s %s(s) for the given product code [%s].";
    private static final String UPDATE_TIME_QUERY_PARAM = "updateTimeParam";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String MKT_MEDIAS_ALL =
            "SELECT {mktm." + ItemModel.PK + "}" +
                    " FROM {" + MarketplaceMediaModel._TYPECODE + " AS mktm" + " }";

    private static final String MKT_MEDIAS_BY_DATE = MKT_MEDIAS_ALL +
            " WHERE {mktm." + MarketplaceMediaModel.UPDATEDATE + "} >= ?" + UPDATE_TIME_QUERY_PARAM;

    @Resource
    private GenericSearchService genericSearchService;

    @Override
    public List<MarketplaceMediaModel> findMarketplaceMediasUpdatedAfterDate(final Date date) {
        final var query = new FlexibleSearchQuery(MKT_MEDIAS_BY_DATE);
        final String formattedDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).format(DATE_TIME_FORMATTER);
        query.addQueryParameter(UPDATE_TIME_QUERY_PARAM, formattedDate);

        final SearchResult<MarketplaceMediaModel> results = getFlexibleSearchService().search(query);
        return results.getResult();
    }

    @Override
    public List<MarketplaceMediaModel> findAllMarketplaceMedias() {
        final SearchResult<MarketplaceMediaModel> results = getFlexibleSearchService().search(new FlexibleSearchQuery(MKT_MEDIAS_ALL));
        return results.getResult();
    }

    @Override
    public Optional<MarketplaceMediaModel> getMarketplaceMediaModelByProductCode(final String productCode) {
        final var codeCondition = GenericCondition.equals(new GenericSearchField(MarketplaceMediaModel._TYPECODE, MarketplaceMediaModel.PRODUCTCODE), productCode);
        final var query = new GenericQuery(MarketplaceMediaModel._TYPECODE, codeCondition);
        final var medias = genericSearchService.<MarketplaceMediaModel>search(query).getResult();

        if (medias.size() > 1) {
            throw new AmbiguousIdentifierException(format(ERROR_MESSAGE, medias.size(), MarketplaceMediaModel._TYPECODE, productCode));
        }

        return medias.stream().findFirst();
    }
}
