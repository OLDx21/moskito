package net.anotheria.moskito.webui.accumulators.api;

import net.anotheria.anoplass.api.APIException;
import net.anotheria.moskito.core.accumulation.AccumulatedValue;
import net.anotheria.moskito.core.accumulation.Accumulator;
import net.anotheria.moskito.core.accumulation.AccumulatorDefinition;
import net.anotheria.moskito.core.accumulation.AccumulatorRepository;
import net.anotheria.moskito.core.config.MoskitoConfigurationHolder;
import net.anotheria.moskito.core.stats.TimeUnit;
import net.anotheria.moskito.webui.accumulators.bean.AccumulatedValuesBean;
import net.anotheria.moskito.webui.shared.api.AbstractMoskitoAPIImpl;
import net.anotheria.util.NumberUtils;
import net.anotheria.util.sorter.DummySortType;
import net.anotheria.util.sorter.SortType;
import net.anotheria.util.sorter.StaticQuickSorter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Implementation of the AccumulatorAPI.
 *
 * @author lrosenberg
 * @since 13.02.13 18:13
 */
public class AccumulatorAPIImpl extends AbstractMoskitoAPIImpl implements AccumulatorAPI {

	/**
	 * This constant is used to put different timestamp in context.
	 */
	private static final long MINUTE = 1000L*60;

	/**
	 * Object for caching of the sort types.
	 */
	private static final SortType SORT_TYPE = new DummySortType();

	@Override
	public AccumulatorDefinitionAO createAccumulator(AccumulatorPO po) throws APIException{
		AccumulatorDefinition ad = new AccumulatorDefinition();
		ad.setName(po.getName());
		ad.setProducerName(po.getProducerId());
		ad.setStatName(po.getStatName());
		ad.setValueName(po.getValueName());
		ad.setIntervalName(po.getInterval());
		if (po.getUnit()==null || po.getUnit().length()==0)
			ad.setTimeUnit(TimeUnit.MILLISECONDS); //set to default
		else
			ad.setTimeUnit(TimeUnit.fromString(po.getUnit()));
		Accumulator acc = AccumulatorRepository.getInstance().createAccumulator(ad);
		return getAccumulatorDefinition(acc.getId());
	}

	@Override
	public void removeAccumulator(String id) throws APIException {
		AccumulatorRepository.getInstance().removeById(id);
	}

	@Override public AccumulatorAO getAccumulator(String id) throws APIException{
		return new AccumulatorAO(AccumulatorRepository.getInstance().getById(id));
	}

	@Override public AccumulatorAO getAccumulatorByName(String name) throws APIException{
		Accumulator acc = AccumulatorRepository.getInstance().getByName(name);
		if (acc==null)
			throw new IllegalArgumentException("Attempt to access non existing accumulator with name: '" +name+"'");
		return new AccumulatorAO(acc);
	}

	public AccumulatorDefinitionAO getAccumulatorDefinition(String accId) throws APIException{
		Accumulator a = AccumulatorRepository.getInstance().getById(accId);
		AccumulatorDefinitionAO bean = new AccumulatorDefinitionAO();

		bean.setName(a.getName());
		bean.setPath(a.getDefinition().describe());
		bean.setId(a.getId());
		List<AccumulatedValue> values = a.getValues();
		if (values!=null && values.size()>0){
			bean.setNumberOfValues(values.size());
			bean.setLastValueTimestamp(values.get(values.size()-1).getISO8601Timestamp());
		}else{
			bean.setNumberOfValues(0);
			bean.setLastValueTimestamp("none");
		}
		return bean;
	}

	@Override
	public AccumulatedSingleGraphAO getAccumulatorGraphData(String id) throws APIException {
		Accumulator accumulator = AccumulatorRepository.getInstance().getById(id);
		AccumulatedSingleGraphAO singleGraphDataBean = new AccumulatedSingleGraphAO(accumulator.getName());

		singleGraphDataBean.setData(new AccumulatorAO(accumulator).getValues());
		singleGraphDataBean.setColor(MoskitoConfigurationHolder.getConfiguration().getAccumulatorsConfig().getAccumulatorColor(accumulator.getName()));
		return singleGraphDataBean;
	}

	@Override
	public List<AccumulatorDefinitionAO> getAccumulatorDefinitions() throws APIException {
		List<Accumulator> accumulators = AccumulatorRepository.getInstance().getAccumulators();
		List<AccumulatorDefinitionAO> ret = new ArrayList<AccumulatorDefinitionAO>();

		for (Accumulator a : accumulators){
			AccumulatorDefinitionAO bean = new AccumulatorDefinitionAO();

			bean.setName(a.getName());
			bean.setPath(a.getDefinition().describe());
			bean.setId(a.getId());
			bean.setMaxNumberOfValues(a.getDefinition().getMaxAmountOfAccumulatedItems());
			List<AccumulatedValue> values = a.getValues();
			if (values!=null && values.size()>0){
				bean.setNumberOfValues(values.size());
				bean.setLastValueTimestamp(values.get(values.size()-1).getISO8601Timestamp());
			}else{
				bean.setNumberOfValues(0);
				bean.setLastValueTimestamp("none");
			}

			ret.add(bean);
		}

		ret = StaticQuickSorter.sort(ret, SORT_TYPE);
		return ret;

	}

	@Override
	public MultilineChartAO getNormalizedAccumulatorGraphData(List<String> ids) throws APIException {
		return getAccumulatorGraphData(ids, true);
	}

	@Override
	public MultilineChartAO getCombinedAccumulatorGraphData(List<String> ids) throws APIException {
		return getAccumulatorGraphData(ids, false);
	}

	public MultilineChartAO getAccumulatorGraphData(List<String> ids, boolean normalized) throws APIException {

		int normalizeBase = 100;
		int maxValues = 200;

		if (ids.size() == 0)
			throw new APIException("No accumulators selected");

		List<AccumulatedValueAO> dataBeans = new ArrayList<AccumulatedValueAO>();
		List<AccumulatedSingleGraphAO> singleGraphDataBeans = new ArrayList<AccumulatedSingleGraphAO>(ids.size());

		//prepare values
		HashMap<Long, AccumulatedValuesBean> values = new HashMap<Long, AccumulatedValuesBean>();
		List<String> accNames = new ArrayList<String>();

		for (String id : ids){
			AccumulatorAO acc = getAccumulator(id);
			AccumulatedSingleGraphAO singleGraphDataBean = getAccumulatorGraphData(id);
			singleGraphDataBeans.add(singleGraphDataBean);

			accNames.add(acc.getName());
			List<AccumulatedValueAO> accValues = acc.getValues();
			for (AccumulatedValueAO v : accValues){
				long timestamp = v.getNumericTimestamp();
				timestamp = timestamp /  MINUTE * MINUTE;
				AccumulatedValuesBean bean = values.get(timestamp);
				if (bean==null){
					bean = new AccumulatedValuesBean(timestamp);
					values.put(timestamp, bean);
				}
				bean.setValue(acc.getName(), v.getFirstValue());

			}
		}
		List<AccumulatedValuesBean> valuesList = StaticQuickSorter.sort(values.values(), SORT_TYPE);

		//now check if the data is complete
		//Stores last known values to allow filling in of missing values (combining 1m and 5m values)
		HashMap<String, String> lastValue = new HashMap<String, String>();

		//filling last (or first) values.
		for (String accName : accNames){
			//first put 'some' initial value.
			lastValue.put(accName, "0");
			//now search for first non-null value
			for(AccumulatedValuesBean accValueBean : valuesList){
				String aValue = accValueBean.getValue(accName);
				if (aValue!=null){
					lastValue.put(accName, aValue);
					break;
				}
			}
		}

		for(AccumulatedValuesBean accValueBean : valuesList){
			for (String accName : accNames){
				String value = accValueBean.getValue(accName);
				if (value==null){
					accValueBean.setValue(accName, lastValue.get(accName));
				}else{
					lastValue.put(accName, value);
				}
			}
		}

		if (normalized){
			normalize(valuesList, accNames, normalizeBase);
		}

		//now create final data
		for(AccumulatedValuesBean avb : valuesList){
			AccumulatedValueAO bean = new AccumulatedValueAO(avb.getTime());
			bean.setIsoTimestamp(NumberUtils.makeISO8601TimestampString(avb.getTimestamp()));
			bean.setNumericTimestamp(avb.getTimestamp());

			for (String accName : accNames){
				bean.addValue(avb.getValue(accName));
			}
			dataBeans.add(bean);
		}

		//generally its not always a good idea to use subList, but since that list isn't reused,
		//as in subList or subList of subList, its ok.
		if (dataBeans.size()>maxValues)
			dataBeans = dataBeans.subList(dataBeans.size()-maxValues, dataBeans.size());

		MultilineChartAO ret = new MultilineChartAO();
		ret.setData(dataBeans);
		ret.setNames(accNames);
		ret.setSingleGraphAOs(singleGraphDataBeans);
		return ret;
	}


	/*test visibility */ static void normalize(List<AccumulatedValuesBean> values, List<String> names, int limit){
		for (String name : names){
			//System.out.println("normalizing "+name);
			ArrayList<Float> valueCopy = new ArrayList<Float>(values.size());
			//step1 transform everything to float
			float min = Float.MAX_VALUE, max = Float.MIN_VALUE;
			for (AccumulatedValuesBean v : values){
				float val = Float.parseFloat(v.getValue(name));
				if (val>max)
					max = val;
				if (val<min)
					min = val;
				valueCopy.add(val);
			}
			//System.out.println("1: "+valueCopy);
			float range = max - min;
			float multiplier = limit / range;
			//System.out.println("range "+range+", multiplier "+multiplier);

			//step2 recalculate
			for (int i=0; i<values.size(); i++){
				float newValue = (valueCopy.get(i)-min)*multiplier;
				values.get(i).setValue(name, ""+newValue);
			}
		}
	}


}
