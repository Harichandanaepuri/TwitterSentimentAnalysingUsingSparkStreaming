name := "kafka"

version := "0.1"

scalaVersion := "2.11.8"

val sparkVersion = "2.2.1"


lazy val root = (project in file(".")).
  settings(
    name := "kafka",
    version := "1.0",
    scalaVersion := "2.11.8",
    mainClass in Compile := Some("kafka")
  )

resolvers += Resolver.url("SparkPackages", url("https://dl.bintray.com/spark-packages/maven/"))

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-library" % "2.11.8",
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" %sparkVersion,
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion,
  "org.apache.spark" %% "spark-mllib" % sparkVersion,
  "org.apache.bahir" %% "spark-streaming-twitter" % "2.3.2",
  // https://mvnrepository.com/artifact/graphframes/graphframes
  "graphframes" % "graphframes" % "0.7.0-spark2.3-s_2.11",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.9.2" artifacts (Artifact("stanford-corenlp", "models"), Artifact("stanford-corenlp"))
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}