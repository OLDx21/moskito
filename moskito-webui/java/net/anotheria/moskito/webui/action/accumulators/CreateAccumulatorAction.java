package net.anotheria.moskito.webui.action.accumulators;

import net.anotheria.maf.action.ActionCommand;
import net.anotheria.maf.action.ActionMapping;
import net.anotheria.maf.bean.FormBean;
import net.anotheria.moskito.core.accumulation.AccumulatorDefinition;
import net.anotheria.moskito.core.accumulation.AccumulatorRepository;
import net.anotheria.moskito.core.stats.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This action creates a new accumulator.
 * @author lrosenberg
 */
public class CreateAccumulatorAction extends BaseAccumulatorsAction{
	@Override
	public ActionCommand execute(ActionMapping mapping, FormBean formBean, HttpServletRequest req, HttpServletResponse res) {
		String producerId = req.getParameter(PARAM_PRODUCER_ID);
		String valueName = req.getParameter(PARAM_VALUE_NAME);
		String statName = req.getParameter(PARAM_STAT_NAME);
		String intervalName = req.getParameter(PARAM_INTERVAL);
		String unitName = req.getParameter(PARAM_UNIT);
		String accName = req.getParameter(PARAM_NAME);

		AccumulatorDefinition ad = new AccumulatorDefinition();
		ad.setName(accName);
		ad.setProducerName(producerId);
		ad.setStatName(statName);
		ad.setValueName(valueName);
		ad.setIntervalName(intervalName);
		ad.setTimeUnit(TimeUnit.fromString(unitName));
		AccumulatorRepository.getInstance().createAccumulator(ad);

		return mapping.redirect();
	}
}
