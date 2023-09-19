package bench

import java.util.concurrent.TimeUnit
import jdk.incubator.vector.LongVector
import org.openjdk.jmh.annotations.*

const val WARMUP_ITERATIONS = 5

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = WARMUP_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = ["--add-modules=jdk.incubator.vector"])
class JvmTestBenchmark {

  private var data = 0.0

  @Setup
  fun setUp() {
    data = 3.0
  }

  @Benchmark
  fun sqrtBenchmark(): Double {
    return Math.sqrt(data)
  }

  @Benchmark
  fun cosBenchmark(): Double {
    return Math.cos(data)
  }

  @Benchmark fun vectorAPI() = LongVector.SPECIES_PREFERRED.length()
}
