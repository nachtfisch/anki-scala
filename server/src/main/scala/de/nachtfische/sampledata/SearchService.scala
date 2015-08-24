package de.nachtfische.sampledata

import ankiscala.services.FlashCard
import de.nachtfische.CommonConst
import org.elasticsearch.action.index.IndexRequestBuilder
import org.elasticsearch.common.settings.{ImmutableSettings, Settings}
import org.elasticsearch.node.{NodeBuilder, Node}


object SearchService {

    val settings:Settings = ImmutableSettings
      .settingsBuilder()
      .put("node.http.enabled", true)
      .put("path.data", CommonConst.PROJECT_PATH + "target/data")
      .put("path.logs", CommonConst.PROJECT_PATH + "target/data")
      .put("index.gateway.type", "none")
      .put("index.store.type", "memory")
      .put("index.number_of_shards", 1)
      .put("index.number_of_replicas", 0)

      .build();
    val node:Node = NodeBuilder.nodeBuilder().settings(settings).node();
    val client = node.client()



    def search(query:String): List[String] = {
        val builder: IndexRequestBuilder = new IndexRequestBuilder(client)
        .setId("some")

        List("some", "someOther")
    }

    def index(flashCard:FlashCard) = {

    }

}
