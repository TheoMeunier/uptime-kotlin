package tmenier.fr

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection(targets = [PropertyNamingStrategies.SnakeCaseStrategy::class])
class NativeConfig
