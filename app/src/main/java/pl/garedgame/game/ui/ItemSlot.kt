package pl.garedgame.game.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.view_item_slot.view.*
import pl.garedgame.game.Game
import pl.garedgame.game.GameApplication
import pl.garedgame.game.R
import pl.garedgame.game.extensions.setBounceOnClick
import pl.garedgame.game.objects.subOjects.*
import pl.garedgame.game.util.setOnSingleOnMeasured

class ItemSlot @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr) {

    var isActive = false
        set(value) {
            field = value
            setBackgroundResource(
                    if (value) R.drawable.primary_dark_framed
                    else R.drawable.primary_framed
            )
        }

    var slotType: Slot? = null

    init {
        LinearLayout.inflate(context, R.layout.view_item_slot, this)
        setItem(null)
        post { isActive = false }
    }

    fun setItemByKey(itemKey: String?, hint: String = "") {
        setItem(itemKey?.let { Game.content.templateEquipment(it) }, hint)
    }

    fun setItemStack(itemKey: String, count: Int) {
        setItemByKey(itemKey)
        val name = Game.content.templateEquipment(itemKey).name
        nameAndCount.text = resources.getString(R.string.equipment,
                name, count.toString())
    }

    private fun prepareTooltipText(item: Equipment): String {
        var attributes = ""
        item.also {
            when (it) {
                is WeaponMelee -> {
                    attributes += "\ndmg=${it.minDamage}-${it.maxDamage}"
                    attributes += "\nrange=${it.range}"
                }
                is WeaponRange -> {
                    attributes += "\ndmg=${it.bulletDamage}*${it.bulletCount}"
                    attributes += "\nspread angle=${it.spreadAngle}"
                }
                is Armor -> {
                    attributes += "\narmor=${it.armor}"
                    attributes += "\nspeed=${it.speed}"
                }
                is Explosive -> {
                    attributes += "\ndmg=${it.damage}"
                    attributes += "\nrange=${it.range}"
                }
                is Usable -> {
                    attributes += "\nloads=${it.loads}"
                    it.health?.let { health ->  attributes += "\nheal=${health}" }
                }
            }
        }
        return "${item.name}:\n$attributes\n${item.description}"
    }

    fun setItem(item: Equipment?, hint: String = "") {
        if (item != null) {
            setOnSingleOnMeasured {
                itemImage.setImageDrawable(GameApplication.instance.getAssetsDrawableByWidth(
                        item.image, (measuredWidth * 0.8f).toInt()
                ))
            }
            hintLabel.visibility = View.INVISIBLE
            itemImage.visibility = View.VISIBLE
            infoButton.visibility = View.VISIBLE
            infoButton.setBounceOnClick {
                Toast.makeText(context, prepareTooltipText(item), Toast.LENGTH_LONG).show()
            }
            nameAndCount.visibility = View.VISIBLE
            nameAndCount.text = item.name
        } else {
            hintLabel.visibility = View.VISIBLE
            hintLabel.text = hint
            itemImage.visibility = View.INVISIBLE
            infoButton.visibility = View.INVISIBLE
            nameAndCount.visibility = View.INVISIBLE
        }
    }
}
