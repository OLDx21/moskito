package net.anotheria.moskito.core.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.configureme.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is configuration holder object for the MoskitoConfiguration. Currently the reconfiguration option of
 *
 * @author lrosenberg
 * @since 2.0
 */
public enum MoskitoConfigurationHolder {
	/**
	 * Singleton instance.
	 */
	INSTANCE;

	/**
	 * Logger.
	 */
	private final Logger log;

	/**
	 * Configuration.
	 */
	private volatile MoskitoConfiguration configuration;

	/**
	 * Creates a new configuration holder.
	 */
	MoskitoConfigurationHolder(){
		log = LoggerFactory.getLogger(MoskitoConfigurationHolder.class);
		configuration = createDefaultConfiguration();
		try{
			//now let configuration override some config options.
			ConfigurationManager.INSTANCE.configure(configuration);
		}catch(IllegalArgumentException e){
			log.info("MoSKito configuration not found, working with default configuration, visit https://confluence.opensource.anotheria.net/display/MSK/Configuration+Guide for more details.");
		}
	}

	/**
	 * Creates default configuration in case there is no moskito.json supplied with the project.
	 * We prefer to have all default configuration at one place to have a better overview, instead of having the default values in the objects themself.
	 * @return
	 */
	private static MoskitoConfiguration createDefaultConfiguration(){
		MoskitoConfiguration config = new MoskitoConfiguration();

		config.getThresholdsAlertsConfig().getAlertHistoryConfig().setMaxNumberOfItems(200);
		config.getThresholdsAlertsConfig().getAlertHistoryConfig().setToleratedNumberOfItems(220);

		//NotificationProviderConfig[] providers = new NotificationProviderConfig[1];
		//providers[0] = new NotificationProviderConfig();
		//providers[0].setClassName(LogFileNotificationProvider.class.getName());
		//providers[0].setProperty("appenderName", "MoskitoAlert");

		//The default size for the threadpool for alert dispatching. This threadpool is needed to prevent app from being blocked by a slow alert notification processor.
		//Default value is 1. Increase it if you have many alerts and many notification providers.
		config.getThresholdsAlertsConfig().setDispatcherThreadPoolSize(1);

		//default number of values in an accumulaotr.
		config.getAccumulatorsConfig().setAccumulationAmount(200);

		return config;
	}

	/**
	 * Returns current configuration.
	 * @return
	 */
	public static MoskitoConfiguration getConfiguration(){
		return INSTANCE.configuration;
	}

	/**
	 * This method allows to set configuration from outside and is solely for testing purposes (junit). Do not use otherwise please.
	 * @param configuration
	 */
	@SuppressFBWarnings("ME_ENUM_FIELD_SETTER")
	public void setConfiguration(MoskitoConfiguration configuration){
		this.configuration = configuration;
	}

	/**
	 * Used for junit tests.
	 */
	public static void resetConfiguration(){
		INSTANCE.configuration = createDefaultConfiguration();
	}



}
