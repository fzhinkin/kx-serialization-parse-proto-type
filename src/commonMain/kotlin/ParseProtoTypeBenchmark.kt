package kotlinx.serialization.benchmark

import kotlinx.benchmark.*
import kotlin.random.Random

enum class ProtoWireType(val typeId: Int) {
    INVALID(-1),
    VARINT(0),
    i64(1),
    SIZE_DELIMITED(2),
    i32(5),
    ;

    companion object {
        private val staticEntries = Array(8) { typeId ->
            ProtoWireType.entries.find { it.typeId == typeId } ?: INVALID
        }

        private val shortEntries = Array(6) { typeId ->
            ProtoWireType.entries.find { it.typeId == typeId } ?: INVALID
        }

        fun fromDefaultImpl(typeId: Int): ProtoWireType {
            return ProtoWireType.entries.find { it.typeId == typeId } ?: INVALID
        }

        fun fromArray(typeId: Int): ProtoWireType {
            if (typeId < 0 || typeId >= staticEntries.size) return INVALID
            return staticEntries[typeId]
        }

        fun fromShortArray(typeId: Int): ProtoWireType {
            if (typeId < 0 || typeId >= shortEntries.size) return INVALID
            return shortEntries[typeId]
        }

        fun fromArrayLowerBits(typeId: Int): ProtoWireType {
            return staticEntries[typeId and 7]
        }

        fun fromSwitch(typeId: Int): ProtoWireType = when (typeId) {
            VARINT.typeId -> VARINT
            i64.typeId -> i64
            SIZE_DELIMITED.typeId -> SIZE_DELIMITED
            i32.typeId -> i32
            else -> INVALID
        }
    }
}

@Warmup(iterations = 5, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
open class ParseProtoTypeBenchmark {
    private val ids = IntArray(128) {
        Random.nextInt(0, 255)
    }

    @Benchmark
    fun baselineImpl(bh: Blackhole) {
        ids.forEach {
            bh.consume(ProtoWireType.fromDefaultImpl(it and 7))
        }
    }

    @Benchmark
    fun switchImpl(bh: Blackhole) {
        ids.forEach {
            bh.consume(ProtoWireType.fromSwitch(it and 7))
        }
    }

    @Benchmark
    fun arrayImpl(bh: Blackhole) {
        ids.forEach {
            bh.consume(ProtoWireType.fromArray(it and 7))
        }
    }

    @Benchmark
    fun shortArrayImpl(bh: Blackhole) {
        ids.forEach {
            bh.consume(ProtoWireType.fromShortArray(it and 7))
        }
    }

    @Benchmark
    fun arrayWithLowerBitsImpl(bh: Blackhole) {
        ids.forEach {
            bh.consume(ProtoWireType.fromArrayLowerBits(it))
        }
    }
}
