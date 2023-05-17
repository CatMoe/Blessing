package catmoe.fallencrystal.moefilter.common.check

interface ICheck {
    fun isDenied(address: String): BlockedType

    fun activeType(): EnableType
}