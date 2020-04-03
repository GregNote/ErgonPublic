package pl.garedgame.game.fragments

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import kotlinx.android.synthetic.main.fragment_behavior.*
import pl.garedgame.game.Game
import pl.garedgame.game.R
import pl.garedgame.game.adapters.RecyclerAdapter
import pl.garedgame.game.adapters.RecyclerViewHolder
import pl.garedgame.game.behavior.*
import pl.garedgame.game.util.px

class BehaviorFragment: Fragment() {
    private var unitId = -1L
    val gson: Gson
    var behaviorAdapter = BehaviorAdapter()

    init {
        val behaviorFactory = RuntimeTypeAdapterFactory
                .of(BTNode::class.java, "nodeType")
                .registerSubtype(BTComposite::class.java, "composite")
                .registerSubtype(BTFilter::class.java, "filter")
                .registerSubtype(BTSelector::class.java, "selector")
                .registerSubtype(BTParallel::class.java, "parallel")
                .registerSubtype(BTBoolean::class.java, "boolean")
                .registerSubtype(BTRecon::class.java, "recon")
                .registerSubtype(BTAttackNearestEnemy::class.java, "attackNearestEnemy")
                .registerSubtype(BTFollowLeader::class.java, "followLeader")
        gson = GsonBuilder()
                .registerTypeAdapterFactory(behaviorFactory)
                .excludeFieldsWithoutExposeAnnotation().create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_behavior, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        unitId = arguments?.getLong("unitId", unitId) ?: unitId
        nodes.layoutManager = LinearLayoutManager(context)
        behaviorAdapter.root = getUnitBehaviorRoot()
        nodes.adapter = behaviorAdapter
    }

    private fun getUnitBehaviorRoot(): BTNode? {
        val unit = Game.instance.findGameUnitById(unitId)
        return unit?.behaviorRoot
    }

    private fun changeUnitBehaviorRoot(root: BTNode) {
        val unit = Game.instance.findGameUnitById(unitId)
        unit?.changeBehaviorRoot(root)
    }

    class BehaviorViewHolder(private val textView: TextView) : RecyclerViewHolder<BTNode>(textView) {
        override fun bind(item: BTNode?) {
            textView.text = item?.javaClass?.name
        }
        override fun clear() { }
    }

    class BehaviorAdapter(var root: BTNode? = null) : RecyclerAdapter<BTNode, BehaviorViewHolder>() {

        override val items: ArrayList<BTNode>
            get() = arrayListOf<BTNode>().apply {
                root?.also {
                    add(it)
                }
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BehaviorViewHolder {
            val textView = TextView(ContextThemeWrapper(parent.context, R.style.NormalButton))
            textView.setPadding(2.px, 2.px, 2.px, 2.px)
            textView.post {
                textView.layoutParams?.also {
                    it.width = 120.px
                    textView.layoutParams = it
                }
            }
            return BehaviorViewHolder(textView)
        }
    }
}
