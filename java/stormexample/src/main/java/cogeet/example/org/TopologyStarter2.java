package cogeet.example.org;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;

public class TopologyStarter2 {
	public static void main(String[] args) {
		System.setProperty("logfile.name", "localcluster.log");
		String log4jConfigFile = System.getProperty("storm.home")
				+ "/logback/cluster.xml";
		LoggerContext context = (LoggerContext) LoggerFactory
				.getILoggerFactory();
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(context);
		try {
			context.reset();
			configurator.doConfigure(log4jConfigFile);
		} catch (JoranException e) {
			e.printStackTrace();
		}

		System.setProperty("java.io.tmpdir", "/tmp/stormtmp");
		System.setProperty("storm.conf.file", "storm.yaml");
		TopologyBuilder tbuilder = new TopologyBuilder();
		tbuilder.setSpout("Spout", new SampleSpout(), 16);
		Fields flds1 = new Fields(Consts.SPOUT_FIELD_1);
		tbuilder.setBolt("bolta", new SampleBoltA(), 12).fieldsGrouping(
				"Spout", flds1);
		tbuilder.setBolt("boltb", new SampleBoltB(), 8)
				.shuffleGrouping("bolta");
		tbuilder.setBolt("boltd", new SampleBoltD(), 16)
				.shuffleGrouping("boltb").shuffleGrouping("Spout", "dstream");

		Config conf = new Config();
		conf.setMaxTaskParallelism(128);
		conf.setNumWorkers(2);
		conf.setNumAckers(2);
		conf.setDebug(false);
		conf.put(Config.STORM_LOCAL_DIR, "/tmp/stormtmp");
		conf.put(Config.TOPOLOGY_DEBUG, false);
		conf.registerSerialization(Sample.class, SampleSerializer.class);
		conf.registerSerialization(Sample2.class, Sample2Serializer.class);
		conf.registerSerialization(GroupingKey.class,
				GroupingKeySerializer.class);
		conf.setMaxSpoutPending(64);
		conf.setMessageTimeoutSecs(30);
		Config.setMaxSpoutPending(conf, 6000);
		if (System.getenv("runlocal") != null) {
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("AnotherTopology", conf,
					tbuilder.createTopology());
		} else {
			try {
				StormSubmitter.submitTopology("AnotherTopology", conf,
						tbuilder.createTopology());
			} catch (AlreadyAliveException e) {
				e.printStackTrace();
			} catch (InvalidTopologyException e) {
				e.printStackTrace();
			}
		}
	}
}
