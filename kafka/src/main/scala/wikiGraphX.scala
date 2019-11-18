import org.apache.spark.{SparkConf, SparkContext}
import org.graphframes.GraphFrame
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.Encoders
import org.apache.spark.sql.functions.desc

object wikiGraphX {
  case class WikiSchema(from: String, to:String)
  def main(args: Array[String]): Unit = {
    System.setProperty("hadoop.home.dir", if (args.length > 2) args(2) else "C:\\Users\\kavya\\Downloads\\kafka\\a")
    val inputPath =  if (args.length > 0) args(0) else "C:\\Users\\kavya\\Downloads\\kafka\\src\\main\\Wiki-Vote.txt"
    val output = if (args.length > 1) args(1) else "C:\\Users\\kavya\\Downloads\\kafka\\output"

    val conf = new SparkConf().setAppName("graphX").set("spark.driver.allowMultipleContexts", "true").setMaster("local")
    val sc = new SparkContext(conf).setCheckpointDir("/checkpoint")
    val spark = SparkSession.builder()
      .master("local[*]")
      .getOrCreate()


    val wikiSchema = Encoders.product[WikiSchema].schema
    val df = spark.read.format("csv").option("header","true").option("delimiter","\t").schema(wikiSchema).load(inputPath)
      .toDF("src","dst")

    val srcArr = df.select("src").toDF("id")
    val dstArr = df.select("dst").toDF("id")
    val nodes = srcArr.union(dstArr).distinct()

    val wikiGraph = GraphFrame(nodes, df)

    //a.top 5 nodes with highest outdegree
    val outDeg = wikiGraph.outDegrees
    val outDegTmp = outDeg.orderBy(desc("outDegree")).select("id","outDegree").limit(5)
    outDegTmp.rdd.map(_.toString()).saveAsTextFile(output+"\\outDeg")

    //b.top 5 nodes with highest indegree
    val inDeg = wikiGraph.inDegrees
    val inDegTmp = inDeg.orderBy(desc("inDegree")).select("id", "inDegree").limit(5)
    inDegTmp.rdd.map(_.toString()).saveAsTextFile(output+"\\inDeg")

    //c.pagerank for each node and ouput top 5 nodes with highest pagerank
    val ranks = wikiGraph.pageRank.resetProbability(0.15).maxIter(10).run()
    val ranksTmp = ranks.vertices.orderBy(desc("pagerank")).select("id", "pagerank").limit(5)
    ranksTmp.rdd.map(_.toString()).saveAsTextFile(output+"\\ranks")

    //d. connected components algorithm
    val minGraph = GraphFrame(wikiGraph.vertices, wikiGraph.edges.sample(false, 0.1))
    val cc = minGraph.connectedComponents.run()
    val ccTmp = cc.groupBy("component").count.orderBy(desc("count")).limit(5)
    ccTmp.rdd.map(_.toString()).saveAsTextFile(output+"\\ConnectedComponents")

    //e. triangle counts algorithm
    wikiGraph.triangleCount.run().orderBy(desc("count")).limit(5).rdd.map(_.toString()).saveAsTextFile(output+"\\triangleCounts")
  }
}
