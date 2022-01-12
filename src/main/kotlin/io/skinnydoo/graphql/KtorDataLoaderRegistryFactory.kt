package io.skinnydoo.graphql

import com.expediagroup.graphql.server.execution.DataLoaderRegistryFactory
import org.dataloader.DataLoaderRegistry

/**
 * Example of how to register the various DataLoaders using [DataLoaderRegistryFactory]
 */
class KtorDataLoaderRegistryFactory : DataLoaderRegistryFactory {

  override fun generate(): DataLoaderRegistry {
    val registry = DataLoaderRegistry()
    return registry
  }
}
