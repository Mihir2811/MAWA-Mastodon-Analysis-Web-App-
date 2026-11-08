import com.google.inject.AbstractModule;
import services.AuthorAnalysisService;
import services.AuthorAnalysisServiceImpl;
import services.MastodonClient;
import services.MastodonClientImpl;
import services.MastodonService;
import services.MastodonServiceImpl;
import services.SentimentAnalysisService;
import services.SentimentAnalysisServiceImpl;
import services.StatisticalAnalysisService;
import services.StatisticalAnalysisServiceImpl;
import services.WordStatsService;
import services.WordStatsServiceImpl;

/**
 * Guice dependency injection configuration module for binding service interfaces to their implementations.
 *
 * @author Kush Chaudhari
 * @author Mihir Vipulbhai Panchal
 * @author Owen Van Esbroeck
 * @author Qiang Xue
 */
public class Module extends AbstractModule {

    /**
     * Configures dependency injection bindings for service interfaces to their implementations.
     *
     * @author Kush Chaudhari
     * @author Mihir Vipulbhai Panchal
     * @author Owen Van Esbroeck
     * @author Qiang Xue
     */
    @Override
    protected void configure() {
        bind(MastodonClient.class).to(MastodonClientImpl.class);
        bind(MastodonService.class).to(MastodonServiceImpl.class);
        bind(SentimentAnalysisService.class).to(SentimentAnalysisServiceImpl.class);
        bind(AuthorAnalysisService.class).to(AuthorAnalysisServiceImpl.class);
        bind(StatisticalAnalysisService.class).to(StatisticalAnalysisServiceImpl.class);
        bind(WordStatsService.class).to(WordStatsServiceImpl.class);
    }
}
