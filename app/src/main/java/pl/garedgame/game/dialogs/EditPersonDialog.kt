package pl.garedgame.game.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_edit_person.*
import pl.garedgame.game.BuildConfig
import pl.garedgame.game.R
import pl.garedgame.game.extensions.setBounceOnClick
import pl.garedgame.game.extensions.setOnClick
import pl.garedgame.game.extensions.setOnLongClick
import pl.garedgame.game.objects.Person
import pl.garedgame.game.objects.subOjects.Equipment
import pl.garedgame.game.objects.subOjects.Profession
import pl.garedgame.game.objects.subOjects.Slot
import pl.garedgame.game.ui.ItemSlot
import pl.garedgame.game.util.px

class EditPersonDialog(context: Context, listener: EditPersonDialogListener)
    : Dialog(context) {

    private val equipmentAdapter = EquipmentAdapter()
    private var activeSlot: ItemSlot? = null

    private val person = listener.getPerson()
    private val items = listener.getItems()
    private val navController = listener.getNavController()

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_edit_person)
        avatar.updateColors(person)
        soldierSkill.skill = person.skills.soldierSkill
        soldierSkill.setBounceOnClick(this::onSkillClick)
        engineerSkill.skill = person.skills.engineerSkill
        engineerSkill.setBounceOnClick(this::onSkillClick)
        mechanicSkill.skill = person.skills.mechanicSkill
        mechanicSkill.setBounceOnClick(this::onSkillClick)
        scientistSkill.skill = person.skills.scientistSkill
        scientistSkill.setBounceOnClick(this::onSkillClick)

        editBehavior.visibility = if (BuildConfig.DEBUG) {
            editBehavior.setOnClick {
                listener.onDismiss()
                navController?.navigate(R.id.to_behaviorFragment, Bundle().apply { putLong("unitId", person.id) })
            }
            View.VISIBLE
        } else View.GONE

        weapon1.slotType = Slot.Weapon
        weapon2.slotType = Slot.Weapon
        armor.slotType = Slot.Armor
        usable.slotType = Slot.Utility

        if (items == null) {
            baseEquipment.visibility = View.GONE
        } else {
            weapon1.setBounceOnClick(this::onItemClick)
            weapon1.setOnLongClick(this::onItemLongClick)
            weapon2.setBounceOnClick(this::onItemClick)
            weapon2.setOnLongClick(this::onItemLongClick)
            armor.setBounceOnClick(this::onItemClick)
            armor.setOnLongClick(this::onItemLongClick)
            usable.setBounceOnClick(this::onItemClick)
            usable.setOnLongClick(this::onItemLongClick)
            equipmentAdapter.onClick = { item ->
                if (activeSlot != null) replaceItemInSlot(activeSlot, item)
            }
            baseEquipment.adapter = equipmentAdapter
            baseEquipment.layoutManager = LinearLayoutManager(baseEquipment.context)
        }
        setOnDismissListener {
            listener.onDismiss()
        }
        updateActiveSkill()
        updateEquipment()
    }

    private fun onSkillClick(view: View) {
        when (view) {
            soldierSkill -> person.profession = Profession.Soldier
            engineerSkill -> person.profession = Profession.Engineer
            mechanicSkill -> person.profession = Profession.Mechanic
            scientistSkill -> person.profession = Profession.Scientist
        }
        updateActiveSkill()
    }

    private fun onItemClick(view: View) {
        setNewActiveSlot(view as ItemSlot)
        updateEquipment()
    }

    private fun onItemLongClick(view: View): Boolean {
        return if (items != null) {
            replaceItemInSlot(view, null)
            true
        } else false
    }

    private fun updateEquipment() {
        weapon1.setItem(person.primaryWeapon, weapon1.resources.getString(R.string.weapon1))
        weapon2.setItem(person.secondaryWeapon, weapon2.resources.getString(R.string.weapon2))
        armor.setItem(person.armor, armor.resources.getString(R.string.armor))
        usable.setItem(person.utilityItem, usable.resources.getString(R.string.usable))
        val result = arrayListOf<Equipment>()
        activeSlot?.also { slot ->
            result.addAll(items.orEmpty().filter { it.slot == slot.slotType })
        }
        equipmentAdapter.update(result)
        updateEquipmentLayout()
    }

    private fun updateEquipmentLayout() {
        val set = ConstraintSet()
        set.clone(editPersonLayout)
        set.constrainWidth(baseEquipment.id, if (activeSlot != null) 150.px else 0)
        set.applyTo(editPersonLayout)
    }

    private fun updateActiveSkill() {
        soldierSkill.active = person.profession == Profession.Soldier
        engineerSkill.active = person.profession == Profession.Engineer
        mechanicSkill.active = person.profession == Profession.Mechanic
        scientistSkill.active = person.profession == Profession.Scientist
    }

    private fun replaceItemInSlot(slot: View?, item: Equipment?) {
        if (items != null) {
            when (slot) {
                weapon1 -> {
                    item?.also { items.remove(it) }
                    person.primaryWeapon?.also { items.add(it) }
                    person.primaryWeapon = item
                }
                weapon2 -> {
                    item?.also { items.remove(it) }
                    person.secondaryWeapon?.also { items.add(it) }
                    person.secondaryWeapon = item
                }
                armor -> {
                    item?.also { items.remove(it) }
                    person.armor?.also { items.add(it) }
                    person.armor = item
                }
                usable -> {
                    item?.also { items.remove(it) }
                    person.utilityItem?.also { items.add(it) }
                    person.utilityItem = item
                }
            }
        }
        updateEquipment()
    }

    private fun setNewActiveSlot(slot: ItemSlot) {
        activeSlot?.isActive = false
        activeSlot = if (slot != activeSlot) slot else null
        activeSlot?.isActive = true
    }

    class EquipmentViewHolder(val itemSlot: ItemSlot) : RecyclerView.ViewHolder(itemSlot)

    class EquipmentAdapter : RecyclerView.Adapter<EquipmentViewHolder>() {
        private val equipment = arrayListOf<Equipment>()
        var onClick: ((Equipment) -> Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentViewHolder {
            val itemSlot = ItemSlot(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            return EquipmentViewHolder(itemSlot)
        }
        override fun getItemCount() = equipment.size
        override fun onBindViewHolder(holder: EquipmentViewHolder, position: Int) {
            val item = equipment[position]
            holder.itemSlot.setItem(item)
            holder.itemSlot.setOnClickListener { onClick?.invoke(item) }
        }
        fun update(list: List<Equipment>) {
            equipment.clear()
            equipment.addAll(list)
            notifyDataSetChanged()
        }
    }

    interface EditPersonDialogListener {
        fun getPerson(): Person
        fun getItems(): ArrayList<Equipment>?
        fun onDismiss()
        fun getNavController(): NavController? = null
    }
}
