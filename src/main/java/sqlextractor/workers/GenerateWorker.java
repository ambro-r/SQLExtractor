package sqlextractor.workers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sqlextractor.objects.Job;
import sqlextractor.objects.Generate;

public class GenerateWorker extends Worker {

    private final Logger LOG = LoggerFactory.getLogger(GenerateWorker.class);

    public GenerateWorker() {}

    private void generateForeignKeys(Job job) {

    }

    public final void work(Job job) {
        for (Generate generate : job.getGenerates()) {
            if("foreignkey".equalsIgnoreCase(generate.getType())) {
                generateForeignKeys(job);
            } else {
                LOG.debug("Generate type \"{}\" not supported.", generate.getType());
            }
        }
    }
}
