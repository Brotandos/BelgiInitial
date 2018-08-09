import BelgiUtil.KEY_BELGI_ID
import BelgiUtil.KEY_OF_TYPE
import BelgiUtil.VALUE_TYPE_BELGI
import BelgiUtil.POLE_MINUS
import BelgiUtil.POLE_PLUS
import BelgiUtil.createId
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.math.absoluteValue

object BelgiUtil {
    const val KEY_OF_TYPE = "type"
    private const val VALUE_TYPE_HANDLED_CONSTRAINT = -1
    const val VALUE_TYPE_BELGI = 0
    const val VALUE_TYPE_QATNAS = 1

    const val POLE_MINUS = false
    const val POLE_PLUS = true

    const val KEY_BELGI_ID = "id"
    const val KEY_QATNAS_TARGET_ID = "targetId"
    const val KEY_QATNAS_TARGET_POLE = "targetPole"
    const val KEY_MARGIN = "margin"

    const val VALUE_EMPTY_QATNAS = -1
    const val VALUE_START_EDGE = 0
    const val VALUE_TOP_EDGE = 1
    const val VALUE_END_EDGE = 2
    const val VALUE_BOTTOM_EDGE = 3

    val createId: Int get() = Random().nextInt().absoluteValue + 1

    fun toBelgiList(markup: String): List<Belgi> {
        val listOfNormalizedJsonArray = wrapToBelgiNorm(markup)
        val belgiToQatnasesList = getBelgiToQatnasesList(listOfNormalizedJsonArray)
        val belgiList = mutableListOf<Belgi>()
        belgiToQatnasesList.forEach { belgiToQatnases ->
            val belgi = belgiToQatnases.first

            belgiToQatnases.second.forEach { qatnasById ->
                val toBelgi = belgiToQatnasesList.find { it.first.id == qatnasById.toBelgiId }?.first
                if (toBelgi != null)
                    belgi.qatnasList += SubjectiveQatnas(qatnasById.fromEdge, toBelgi, qatnasById.toEdge, qatnasById.margin)
                else
                    throw RuntimeException("There is no belgi inside markup with id ${qatnasById.toBelgiId}")
            }

            belgiList += belgi
        }
        return belgiList
    }

    /**
     * Wraps each of line into json's array representation, replaces all '*' characters into empty constraint
     *
     * @param   markup  passed constraint layout's markup
     * @return          normalized for belgi parsing markup
     * */
    private fun wrapToBelgiNorm(markup: String): List<JSONArray> =
            markup
                    .replace("*", empty.toString())
                    .split("\n")
                    .map { JSONArray("[$it]") }

    /**
     * @param   jsonArrays  markup's each line list, wrapped into JSONArray
     * @return              list of pair, first is belgi's json representation, second is its constraints' json representation
     * @see                 Belgi
     * @see                 QatnasById
     * */
    private fun getBelgiToQatnasesList(jsonArrays: List<JSONArray>): List<Pair<Belgi, List<QatnasById>>> {
        val belgiToQatnasesList = mutableListOf<Pair<Belgi, List<QatnasById>>>()

        jsonArrays.forEachIndexed { arrayIndex, jsonArray ->
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                if (jsonObject.isBelgi) {
                    val qatnases = getHorizontalQatnases(jsonArray, i)
                    qatnases += getVerticalQatnases(jsonObject, jsonArrays, arrayIndex)

                    belgiToQatnasesList += Belgi(jsonObject.getInt(KEY_BELGI_ID)) to qatnases
                }
            }
        }

        return belgiToQatnasesList
    }

    /**
     * @param   jsonArray   array of json of markup's exact line
     * @param   index       index of current belgi
     * @return              list of horizontal constraints
     * */
    private fun getHorizontalQatnases(jsonArray: JSONArray, index: Int): MutableList<QatnasById> {
        val horizontalQatnases = mutableListOf<QatnasById>()
        if (index == 0 || index == jsonArray.length()) throw RuntimeException("There must be constraints on both side of belgi!")

        jsonArray.getJSONObject(index - 1).let { leftJsonQatnas ->
            getQatnas(leftJsonQatnas, "left", VALUE_START_EDGE)
                    .let { if (it != null) horizontalQatnases += it }
        }

        jsonArray.getJSONObject(index + 1).let { rightJsonQatnas ->
            getQatnas(rightJsonQatnas, "right", VALUE_END_EDGE)
                    .let { if (it != null) horizontalQatnases += it}
        }

        return horizontalQatnases
    }

    /**
     * @param   jsonBelgi   current json belgi to constraint, need for passing id to find relative position in current json array
     * @param   jsonArrays  all json arrays
     * @param   arrayIndex  current json array's index
     * @return              list of vertical constraints'
     * */
    private fun getVerticalQatnases(jsonBelgi: JSONObject, jsonArrays: List<JSONArray>, arrayIndex: Int): List<QatnasById> {
        val position = getJsonBelgiRelativePosition(jsonArrays[arrayIndex], jsonBelgi.getInt(KEY_BELGI_ID))
        val verticalQatnases = mutableListOf<QatnasById>()

        mutableListOf<Pair<Int, JSONObject>>().let { qatnasesAbove ->
            if (arrayIndex == 0) throw RuntimeException("There must be constraint on top of belgi")
            val topJsonArray = jsonArrays[arrayIndex - 1]
            topJsonArray.forEachObject { index, jsonObject ->
                if (jsonObject.getInt(KEY_OF_TYPE) == VALUE_TYPE_QATNAS)
                    qatnasesAbove += index to jsonObject
            }
            getQatnas(qatnasesAbove[position].second, "above", VALUE_TOP_EDGE, false)
                    .let { if (it != null) verticalQatnases += it }
        }

        mutableListOf<Pair<Int, JSONObject>>().let { qatnasesBelow ->
            // TODO handle if there's no constraint in the bottom
            if (arrayIndex == jsonArrays.size) throw RuntimeException("There must be constraint on bottom of belgi")
            val bottomJsonArray = jsonArrays[arrayIndex + 1]
            bottomJsonArray.forEachObject { index, jsonObject ->
                if (jsonObject.getInt(KEY_OF_TYPE) == VALUE_TYPE_QATNAS)
                    qatnasesBelow += index to jsonObject
            }
            getQatnas(qatnasesBelow[position].second, "below", VALUE_BOTTOM_EDGE, false)
                    .let { if (it != null) verticalQatnases += it }
        }

        return verticalQatnases
    }

    /**
     * This function need for defining vertical constraints. If there're several belgi objects,
     * by defining current belgi's json object's position in json array,
     * you can define position of vertical constraint in line above and below
     *
     * @param   jsonArray   current json array
     * @param   id          current belgi's id
     * @return              position of belgi between all of belgi in json array
     * @see                 getVerticalQatnases
     * */
    private fun getJsonBelgiRelativePosition(jsonArray: JSONArray, id: Int): Int {
        for (i in 0 until jsonArray.length()) {
            var belgiIndex = 0
            val jsonObject = jsonArray.getJSONObject(i)
            if (jsonObject.isBelgi) {
                if (jsonObject.getInt(KEY_BELGI_ID) == id)
                    return belgiIndex

                @Suppress("UNUSED_CHANGED_VALUE")
                belgiIndex++
            }
        }
        return 0
    }


    /**
     * @param   jsonQatnas      json object of constraint
     * @param   sideLabel       label of side, it's need for exception
     * @param   fromEdge        edge, where starts constraint
     * @param   isHorizontal    if constraint is horizontal marker
     * @return                  constraint by id, or null, if it's empty constraint
     * @see                     empty
     * */
    private fun getQatnas(jsonQatnas: JSONObject, sideLabel: String, fromEdge: Int, isHorizontal: Boolean = true): QatnasById? {
        if (jsonQatnas.getInt(KEY_OF_TYPE) != VALUE_TYPE_QATNAS) throw RuntimeException("There must be constraint on the $sideLabel side of belgi")
        if (isHorizontal) jsonQatnas.put(KEY_OF_TYPE, VALUE_TYPE_HANDLED_CONSTRAINT)
        if (jsonQatnas.getInt(KEY_QATNAS_TARGET_ID) == VALUE_EMPTY_QATNAS) return null
        return QatnasById (
                fromEdge,
                jsonQatnas.getInt(KEY_QATNAS_TARGET_ID),
                getEdge(isHorizontal, jsonQatnas.getBoolean(KEY_QATNAS_TARGET_POLE)),
                jsonQatnas.getInt(KEY_MARGIN)
        )
    }

    /**
     * @param   isHorizontal    if constraint must be horizontal
     * @param   isPositivePole  if constraint edge is in positive side (top or end) by x to y axis
     * @return                  edge code
     * @see                     SubjectiveQatnas.fromEdge
     * @see                     QatnasById.fromEdge
     * @see                     QatnasById.toEdge
     * @see                     VALUE_START_EDGE
     * @see                     VALUE_TOP_EDGE
     * @see                     VALUE_END_EDGE
     * @see                     VALUE_BOTTOM_EDGE
     * */
    private fun getEdge(isHorizontal: Boolean, isPositivePole: Boolean): Int =
            if (isHorizontal)
                if (isPositivePole) VALUE_END_EDGE else VALUE_START_EDGE
            else
                if (isPositivePole) VALUE_TOP_EDGE else VALUE_BOTTOM_EDGE

    private val JSONObject.isBelgi: Boolean get() = getInt(KEY_OF_TYPE) == VALUE_TYPE_BELGI
}

open class Belgi(val id: Int) {

    val qatnasList = mutableListOf<SubjectiveQatnas>()

    constructor() : this(createId)

    private val belgiMarker = "$KEY_OF_TYPE : \"$VALUE_TYPE_BELGI\""

    private val attrId = "$KEY_BELGI_ID : \"$id\""

    override fun toString() = """
        { $belgiMarker, $attrId },
    """.trimIndent()

    operator fun minus(margin: Int): Qatnas =
            Qatnas(id, POLE_MINUS, margin)

    operator fun plus(margin: Int): Qatnas =
            Qatnas(id, POLE_PLUS, margin)
}
