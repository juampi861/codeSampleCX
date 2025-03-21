package com.sample.digitalplatform.mirakl.jobs;

import com.sample.digitalplatform.mirakl.services.SampleMiraklExportCatalogService;
import com.sample.digitalplatform.mirakl.service.model.SampleMiraklExportValueListsCronJobModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.media.MediaService;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class SampleMiraklExportValueListsJob extends AbstractJobPerformable<SampleMiraklExportValueListsCronJobModel> {

    private static final Logger LOG = Logger.getLogger(SampleMiraklExportValueListsJob.class);
    private static final String VALUE_LISTS_FILE_PREFIX = "valueLists";
    private static final String VALUE_LISTS_FILE_SUFFIX = "csv";
    private SampleMiraklExportCatalogService sampleMiraklExportCatalogService;
    private MediaService mediaService;

    @Override
    public PerformResult perform(final SampleMiraklExportValueListsCronJobModel sampleMiraklExportValueListsCronJobModel) {
        File file = null;
        try {
            file = File.createTempFile(VALUE_LISTS_FILE_PREFIX, VALUE_LISTS_FILE_SUFFIX);

            try (InputStream inputStream = getMediaService().getStreamFromMedia(sampleMiraklExportValueListsCronJobModel.getValueListsCSVFile())) {
                Files.copy(
                        inputStream,
                        file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }

            this.getsampleMiraklExportCatalogService().sendValueLists(file);

        } catch (final Exception e) {
            LOG.error("An error occurred during the Value Lists export", e);
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
        } finally {
            if (file != null && file.exists()) {
                try {
                    Files.delete(file.toPath());
                } catch (final IOException exception) {
                    LOG.error("An error ocurred deleting temporary value list file");
                }
            }
        }

        LOG.info("Finished Value Lists Export.");
        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

    public SampleMiraklExportCatalogService getSampleMiraklExportCatalogService() {
        return sampleMiraklExportCatalogService;
    }

    public void setSampleMiraklExportCatalogService(final SampleMiraklExportCatalogService sampleMiraklExportCatalogService) {
        this.sampleMiraklExportCatalogService = sampleMiraklExportCatalogService;
    }

    public MediaService getMediaService() {
        return mediaService;
    }

    public void setMediaService(final MediaService mediaService) {
        this.mediaService = mediaService;
    }
}