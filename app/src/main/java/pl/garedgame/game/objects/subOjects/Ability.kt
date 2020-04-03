package pl.garedgame.game.objects.subOjects

import android.view.ContextThemeWrapper
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.annotations.SerializedName
import pl.garedgame.game.GameApplication
import pl.garedgame.game.R
import pl.garedgame.game.adapters.RecyclerAdapter
import pl.garedgame.game.adapters.RecyclerViewHolder
import pl.garedgame.game.util.px

class Ability(val type: Type, val equipment: Equipment? = null) {

    enum class Require {
        None,
        Position,
        Unit
    }

    enum class Type(val texturePath: String, val require: Require) {
        //Items
        @SerializedName("Shoot") Shoot("shoot_ab.png", Require.Position),
        @SerializedName("Melee") Melee("melee_ab.png", Require.Position),
        @SerializedName("Throw") Throw("throw_ab.png", Require.Position),
        @SerializedName("Plant") Plant("plant_ab.png", Require.Position),
        @SerializedName("Use") Use("use_ab.png", Require.Unit),

        //Behaviors
        @SerializedName("Attack") Attack("attack_ab.png", Require.Position),
        @SerializedName("Defence") Defence("defence_ab.png", Require.Position),
        @SerializedName("Recon") Recon("recon_ab.png", Require.None),
        @SerializedName("Patrol") Patrol("patrol_ab.png", Require.Position);
    }
    fun name(): String = when (type) {
        Type.Use -> {
            equipment?.let { "${type.name}\n(${it.name})" } ?: type.name
        }
        else -> type.name
    }
}

class AbilityViewHolder(private val textView: TextView) : RecyclerViewHolder<Ability>(textView) {

    override fun bind(item: Ability?) {
        textView.text = item?.name() ?: ""
        val drawable = item?.let {
            GameApplication.instance.getAssetsDrawableByHeight(it.type.texturePath, 40.px)
        }
        textView.setCompoundDrawablesWithIntrinsicBounds(
                drawable, null, null, null
        )
    }
    override fun clear() { }
}

class AbilityAdapter : RecyclerAdapter<Ability, AbilityViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbilityViewHolder {
        val textView = TextView(ContextThemeWrapper(parent.context, R.style.NormalButton))
        val px1 = 1.px
        textView.compoundDrawablePadding = px1
        textView.setPadding(px1, px1, px1, px1)
        textView.post {
            textView.layoutParams?.also {
                it.width = 120.px
                textView.layoutParams = it
            }
        }
        return AbilityViewHolder(textView)
    }
}
