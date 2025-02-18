package net.anotheria.moskito.core.accumulation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.anotheria.moskito.core.config.MoskitoConfigurationHolder;
import net.anotheria.moskito.core.config.accumulators.AccumulatorConfig;
import net.anotheria.moskito.core.config.accumulators.AccumulatorsConfig;
import net.anotheria.moskito.core.config.accumulators.AutoAccumulatorConfig;
import net.anotheria.moskito.core.helper.AutoTieAbleProducer;
import net.anotheria.moskito.core.helper.TieableDefinition;
import net.anotheria.moskito.core.helper.TieableRepository;
import net.anotheria.moskito.core.producers.IStats;
import net.anotheria.moskito.core.producers.IStatsProducer;
import net.anotheria.moskito.core.stats.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Repository that holds and manages accumulators.
 * @author lrosenberg
 *
 */
public final class AccumulatorRepository<S extends IStats> extends TieableRepository<Accumulator, S> {

	/**
	 * Logger
	 */
	private static Logger log = LoggerFactory.getLogger(AccumulatorRepository.class);

	/**
	* The singleton instance.
	*/
	private static volatile AccumulatorRepository<? extends IStats> INSTANCE;

	/**
	 * Configured autoAccumulatorDefinitions.
	 */
	private List<AutoAccumulatorDefinition> autoAccumulatorDefinitions = new LinkedList<>();



	/**  
	* Returns the singleton instance of the AccumulatorRepository.  
	* @return the one and only instance.  
	*/
	public static AccumulatorRepository<? extends IStats> getInstance(){
		if(INSTANCE == null) {
			INSTANCE = new AccumulatorRepository<>();
		}
		return INSTANCE;
	}


	private AccumulatorRepository(){
		readConfig();
	}

	@Override
	protected boolean tie(Accumulator acc, IStatsProducer<? extends IStats> producer) {
		AccumulatorDefinition definition = acc.getDefinition();
		IStats target = null;
		for (IStats s : producer.getStats()){
			if (s.getName().equals(definition.getStatName())){
				target = s;
				break;
			}
		}
		
		if (target==null){
			if (producer instanceof AutoTieAbleProducer){
				addToAutoTie(acc, producer);
			}else{
				throw new IllegalArgumentException("StatObject not found "+definition.getStatName()+" in "+definition);
			}
		}

		acc.tieToStats(target);
		return true;
		
	}
	
	@Override
	protected Accumulator create(TieableDefinition def){
		return new Accumulator((AccumulatorDefinition)def);
	}
	/**
	 * Returns configured accumulators.
	 * @return
	 */
	public List<Accumulator> getAccumulators(){
		return getTieables();
	}

	public Accumulator createAccumulator(TieableDefinition def){
		return createTieable(def);
	}

	/**
	 * Reads the config and creates configured accumulators. For now this method is only executed on startup.
	 */
	private void readConfig(){
		AccumulatorsConfig config = MoskitoConfigurationHolder.getConfiguration().getAccumulatorsConfig();
		AccumulatorConfig[] acs = config.getAccumulators();
		if (acs!=null && acs.length>0){
			for (AccumulatorConfig ac  : acs){
				AccumulatorDefinition ad = new AccumulatorDefinition();
				ad.setName(ac.getName());
				ad.setIntervalName(ac.getIntervalName());
				ad.setProducerName(ac.getProducerName());
				ad.setStatName(ac.getStatName());
				ad.setTimeUnit(TimeUnit.valueOf(ac.getTimeUnit()));
				ad.setValueName(ac.getValueName());
				Accumulator acc = createAccumulator(ad);
				if (log.isDebugEnabled()){
					log.debug("Created accumulator "+acc);
				}
			}
		}

		AutoAccumulatorConfig[] autoAccumulatorConfigs = config.getAutoAccumulators();
		if (autoAccumulatorConfigs != null && autoAccumulatorConfigs.length>0){
			for (AutoAccumulatorConfig aac : autoAccumulatorConfigs){
				AutoAccumulatorDefinition aad = new AutoAccumulatorDefinition();
				aad.setNamePattern(aac.getNamePattern());
				aad.setProducerNamePattern(aac.getProducerNamePattern());
				aad.setIntervalName(aac.getIntervalName());
				aad.setValueName(aac.getValueName());
				aad.setStatName(aac.getStatName());
				aad.setTimeUnit(TimeUnit.fromString(aac.getTimeUnit()));
				aad.setAccumulationAmount(aac.getAccumulationAmount());
				autoAccumulatorDefinitions.add(aad);
			}
		}

	}

	@Override
	public void notifyProducerUnregistered(IStatsProducer<S> producer) {
		for (Accumulator acc : getTieables()) {
			if (acc.getDefinition().getProducerName().equals(producer.getProducerId())) {
				try {
					//untie accumulator
					acc.tieToStats(null);
					addUntied(acc);
				} catch(Exception e) {
					log.error("notifyProducerUnregistered("+producer+ ')', e );
				}
			}
		}
	}

    /**
     * This method is for unit testing ONLY.
	 * The Findbugs warning is suppressed, because this method is for unit testing only.
     */
	@SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "This method is for unit testing only.")
    void reset() {
        cleanup();
		INSTANCE = new AccumulatorRepository<>();
	}

	/**
	 * This method is for unit testing ONLY.
	 * The Findbugs warning is suppressed, because this method is for unit testing only.
	 */
	@SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "This method is for unit testing only.")
	public static void resetForUnitTests() {
		getInstance().reset();
	}

	@Override
	public void notifyProducerRegistered(IStatsProducer<S> producer) {
		//first allow tieable repository do what it should do.
		super.notifyProducerRegistered(producer);

		String producerId = producer.getProducerId();
		for (AutoAccumulatorDefinition aad : autoAccumulatorDefinitions){
			if (aad.matches(producerId)){
				if (log.isDebugEnabled()){
					log.debug("Creating auto-accumulator out of "+aad+" for "+producerId);
				}
				createAccumulator(aad.toAccumulatorDefinition(producerId));
			}
		}
	}
}
