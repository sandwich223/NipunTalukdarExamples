package cogeet.example.org;

import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;



public class SampleBoltB extends BaseRichBolt {
	private static final long serialVersionUID = 12245546474L;
	private OutputCollector collector = null;
	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;
	}

	@Override
	public void execute(Tuple input) {
		Sample a = (Sample)input.getValueByField(Consts.BOLTA_FIELD_1);
		Sample2 b = (Sample2)input.getValueByField(Consts.BOLTA_FIELD_2);
		collector.emit(input, new Values(a,b));
		collector.ack(input);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields(Consts.BOLTB_FIELD_1, Consts.BOLTB_FIELD_2));
	}

}
