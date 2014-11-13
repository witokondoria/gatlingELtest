package tests

import java.util.Scanner

import io.gatling.core.Predef._
import io.gatling.http.Predef.{ http, jsonPath }
import io.gatling.http.request._
import java.io._

class PrePT extends Simulation with Common{

  object RequiredElement {


    val createDS = exec(http("POST /")
        .post("/")
        .body(ELFileBody("DS.txt")).asJSON
        .check(jsonPath("$.ds.id")
          .saveAs("DSID")))
      .exitHereIfFailed
  }


  val placeholded = new Scanner(new File("../user-files/request-bodies/sources_placeholders.json"));
  val replaced = new PrintWriter("../user-files/request-bodies/sources.json");
  var sources = placeholded.useDelimiter("\\A").next()

  sources = sources.replaceAll("JDBCDS", jdbcds)

  replaced.println(sources);
  placeholded.close();
  replaced.close();

  val feeder = jsonFile("../user-files/request-bodies/sources.json")

  val users = scenario("Pre-requirements")
    .foreach(feeder.records, "record") {
      exec(flattenMapIntoAttributes("${record}"))
      .exec(
        RequiredElement.createDS
      )}

  setUp(
    users
      .inject(atOnceUsers(1))
  ).protocols(httpConf)
}
