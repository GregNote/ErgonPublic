package pl.garedgame.game.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_editor.*
import pl.garedgame.game.Game
import pl.garedgame.game.GameApplication
import pl.garedgame.game.R
import pl.garedgame.game.Vector2
import pl.garedgame.game.adapters.EditorGameObjectAdapter
import pl.garedgame.game.adapters.GameClass
import pl.garedgame.game.adapters.GameObjectAdapter
import pl.garedgame.game.dialogs.SetNameDialog
import pl.garedgame.game.entities.GameObject
import pl.garedgame.game.entities.GameScene
import pl.garedgame.game.entities.Obstacle
import pl.garedgame.game.entities.Terrain
import pl.garedgame.game.extensions.registerOnBackPressedCallback
import pl.garedgame.game.extensions.setBounceOnClick
import pl.garedgame.game.extensions.setOnClick
import pl.garedgame.game.extensions.unregisterOnBackPressedCallback
import pl.garedgame.game.render.ColorSprite
import pl.garedgame.game.render.GameViewGL
import pl.garedgame.game.render.OpenGLRenderer
import pl.garedgame.game.render.TextureRegion
import pl.garedgame.game.skirmish.SceneMap
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

class EditorFragment : Fragment(), SeekBar.OnSeekBarChangeListener {

    enum class ClickAction {
        None,
        AddObject,
    }

    private val directionButtons: Array<TextView> by lazy {
        arrayOf(upBtn, leftBtn, rightBtn, downBtn)
    }

    private val allButtons: Array<TextView> by lazy {
        arrayOf(optionBtn, copyBtn, cancelBtn, deleteBtn, *directionButtons)
    }

    private var clickAction = ClickAction.None
    private var currentObject: GameObject? = null
    private var currentScene: EditorGameScene? = null
    private lateinit var gameObjectsAdapter: GameObjectAdapter
    private lateinit var editorGameObjectsAdapter: EditorGameObjectAdapter

    private fun updateFps(fps: Int) {
        cameraPos?.text = "FPS: $fps\nobjCount: ${gameScene.scene.getObjectsArrayList().size}"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        allButtons.forEach { it.setBounceOnClick(this::onClick) }
        objectOptionsLayout.setOnClick { it.visibility = View.GONE }
        sceneOptionsLayout.setOnClick { it.visibility = View.GONE }

        widthSeekBar.setOnSeekBarChangeListener(this)
        heightSeekBar.setOnSeekBarChangeListener(this)
        rotationSeekBar.setOnSeekBarChangeListener(this)
        gameScene.camera.onClickWithPosition = { pos ->
            when (clickAction) {
                ClickAction.None -> {
                    GameViewGL.post {
                        val objects = gameScene.scene.getObjectsArrayList().filter {
                            Vector2.distance(pos, it.orientation.pos) - max(it.orientation.scaleX, it.orientation.scaleY) <= 2f
                        }.sortedBy { Vector2.distance(pos, it.orientation.pos) }
                        if (objects.isNotEmpty()) view.post { updateCurrentObject(objects[0]) }
                    }
                }
                ClickAction.AddObject -> {
                    GameViewGL.post {
                        gameObjectsAdapter.getSelected()?.also {
                            gameScene.scene.add(
                                    it.instance().apply {
                                        this.orientation.pos.set(pos.rounded())
                                        view.post { updateCurrentObject(this) }
                                    }
                            )
                            view.post { gameObjectsAdapter.clearSelected() }
                        }
                    }
                }
            }
            clickAction = ClickAction.None
        }
        sceneName.setOnClick {
            SetNameDialog(it.context, object: SetNameDialog.SetNameDialogListener {
                override fun getCurrentName(): String = sceneName.text.toString()
                override fun onSetName(name: String) {
                    sceneName.text = name
                }
            }).show()
        }
        newScene.setOnClick {
            GameViewGL.post {
                gameScene.scene = EditorGameScene(32f, 32f, 0.25f).apply {
                    currentScene = this
                }
            }
            updateObjectList()
            sceneOptionsLayout.visibility = View.GONE
        }
        loadScene.setOnClick {
            gameScene.loadEditorScene {
                currentScene = gameScene.scene as EditorGameScene
                updateObjectList()
            }
            sceneOptionsLayout.visibility = View.GONE
        }
        saveSceneToFile.setOnClick {
            val nameOfScene = sceneName.text.toString()
            if (nameOfScene.isEmpty()) {
                SetNameDialog(it.context, object: SetNameDialog.SetNameDialogListener {
                    override fun getCurrentName(): String = nameOfScene
                    override fun onSetName(name: String) {
                        sceneName.text = name
                    }
                }).show()
            } else {
                currentScene?.also { scene ->
                    val json = Game.instance.gson.toJson(SceneMap(scene))
                    GameApplication.instance.writeToFile(json, "${nameOfScene}.txt")
                }
            }
        }
        sceneOptions.setBounceOnClick { sceneOptionsLayout.visibility = View.VISIBLE }

        gameObjectsAdapter = GameObjectAdapter(arrayListOf(
                GameClass("Spawn") { Terrain().apply {
                    tag = "spawn"; texture = "spawn"
                }},
                GameClass("Brown dirt 1") { Terrain().apply {
                    texture = "brown_dirt_1"
                }},
                GameClass("Brown dirt 2") { Terrain().apply {
                    texture = "brown_dirt_2"
                }},
                GameClass("Gray dirt 1") { Terrain().apply {
                    texture = "gray_dirt_1"
                }},
                GameClass("Gray dirt 2") { Terrain().apply {
                    texture = "gray_dirt_2"
                }},
                GameClass("Tiles 1") { Terrain().apply {
                    texture = "tiles_1"
                }},
                GameClass("Tiles 2") { Terrain().apply {
                    texture = "tiles_2"
                }},
                GameClass("Flor") { Terrain().apply {
                    texture = "flor"
                }},
                GameClass("Box") { Obstacle(Obstacle.Type.Low).apply {
                    texture = "box"
                }},
                GameClass("Wall") { Obstacle(Obstacle.Type.High).apply {
                    texture = "wall"
                }},
                GameClass("Gray wall") { Obstacle(Obstacle.Type.High).apply {
                    texture = "gray_wall"
                }}
        ))
        gameObjectsAdapter.onSelected = { _, gameClass ->
            clickAction = if (gameClass != null) ClickAction.AddObject else ClickAction.None
        }
        addableObjects.adapter = gameObjectsAdapter
        addableObjects.layoutManager = LinearLayoutManager(requireContext())

        editorGameObjectsAdapter = EditorGameObjectAdapter()
        editorGameObjectsAdapter.onMoveUp = { _, item ->
            GameViewGL.post {
                if (gameScene.scene.moveTerrainUp(item as Terrain)) {
                    gameScene.post {
                        editorGameObjectsAdapter.updateItems(gameScene.scene.getObjectsArrayList())
                    }
                }
            }
        }
        editorGameObjectsAdapter.onMoveDown = { _, item ->
            GameViewGL.post {
                if (gameScene.scene.moveTerrainDown(item as Terrain)) {
                    gameScene.post {
                        editorGameObjectsAdapter.updateItems(gameScene.scene.getObjectsArrayList())
                    }
                }
            }
        }
        editorGameObjectsAdapter.onClick = { _, item ->
            updateCurrentObject(item)
        }
        gameObjects.adapter = editorGameObjectsAdapter
        gameObjects.layoutManager = LinearLayoutManager(requireContext())
        gameObjectsSwipe.setOnRefreshListener {
            updateObjectList()
            gameObjectsSwipe.isRefreshing = false
        }
        updateCurrentObject()
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) currentObject?.also { obj ->
            when (seekBar) {
                widthSeekBar -> {
                    obj.orientation.scaleX = (progress + 1) * 0.5f
                    seekBar?.post { widthValue.text = "ScaleX: ${obj.orientation.scaleX}" }
                }
                heightSeekBar -> {
                    obj.orientation.scaleY = (progress + 1) * 0.5f
                    seekBar?.post { heightValue.text = "ScaleY: ${obj.orientation.scaleY}" }
                }
                rotationSeekBar -> {
                    obj.orientation.rotation = (progress) * 5f
                    seekBar?.post { rotationValue.text = "R: ${obj.orientation.rotation}" }
                }
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    private fun updateCurrentObject(newObject: GameObject? = null) {
        currentObject = newObject
        currentScene?.currentEditObject = newObject
        changeEnable(
                currentObject != null,
                *directionButtons, optionBtn, cancelBtn, deleteBtn, copyBtn
        )
    }

    override fun onResume() {
        super.onResume()
        gameScene.loadEditorScene {
            currentScene = gameScene.scene as EditorGameScene
            updateObjectList()
        }
        OpenGLRenderer.setFpsListener(this::updateFps)
        registerOnBackPressedCallback {
            if (gameScene.scene.getObjectsArrayList().isNotEmpty()) {
                Game.instance.saveToSceneMap(gameScene.scene)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        OpenGLRenderer.setFpsListener(null)
        unregisterOnBackPressedCallback()
    }

    fun onClick(view: View) {
        when (view) {
            optionBtn -> {
                showOptions()
                clickAction = ClickAction.None
            }
            cancelBtn -> {
                updateCurrentObject()
                updateObjectList()
                clickAction = ClickAction.None
            }
            deleteBtn -> {
                GameViewGL.post {
                    currentObject?.also { obj -> gameScene.scene.remove(obj) }
                    view.post { updateCurrentObject() }
                }
                updateObjectList()
                clickAction = ClickAction.None
            }
            copyBtn -> {
                GameViewGL.post {
                    currentObject?.also { obj ->
                        gameScene.scene.add(
                                GameObject.copy(obj).apply {
                                    view.post { updateCurrentObject(this) }
                                }
                        )
                        view.post { gameObjectsAdapter.clearSelected() }
                    }
                }
                updateObjectList()
            }
            upBtn, leftBtn, rightBtn, downBtn -> {
                currentObject?.also { obj ->
                    if (view == upBtn && obj.orientation.pos.y < gameScene.scene.height / 2f) {
                        obj.orientation.pos.y += 1f; obj.orientation.pos.round()
                    }
                    if (view == leftBtn && gameScene.scene.width / -2f < obj.orientation.pos.x) {
                        obj.orientation.pos.x -= 1f; obj.orientation.pos.round()
                    }
                    if (view == rightBtn && obj.orientation.pos.x < gameScene.scene.width / 2f) {
                        obj.orientation.pos.x += 1f; obj.orientation.pos.round()
                    }
                    if (view == downBtn && gameScene.scene.height / -2f < obj.orientation.pos.y) {
                        obj.orientation.pos.y -= 1f; obj.orientation.pos.round()
                    }
                    updateObjectList()
                }
                clickAction = ClickAction.None
            }
        }
    }

    private fun updateObjectList() {
        gameScene.post {
            editorGameObjectsAdapter.updateItems(gameScene.scene.getObjectsArrayList())
        }
    }

    private fun changeEnable(enable: Boolean, vararg buttons: View) {
        buttons.forEach {
            it.isEnabled = enable
            it.visibility = if (enable) View.VISIBLE else View.GONE
        }
    }

    private fun updateSpinner(obj: GameObject) {
        textureSpinner.also {
            val texturePaths = arrayListOf(">${obj.texture}")
            when (obj) {
                is Obstacle -> {
                    texturePaths.addAll(TextureRegion.getAllRegions().filter { region ->
                        region.texturePath == "objects.png"
                    }.map { region -> region.name })
                }
                is Terrain -> {
                    texturePaths.addAll(TextureRegion.getAllRegions().filter { region ->
                        region.texturePath == "terrain.png"
                    }.map { region -> region.name })
                }
                else -> {
                    texturePaths.addAll(OpenGLRenderer.getAllTextureNames().sorted())
                }
            }
            val dataAdapter = ArrayAdapter<String>(it.context, R.layout.spinner_item_objective,
                    texturePaths)

            dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_objective)
            it.adapter = dataAdapter
            it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) { }
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position > 0) {
                        obj.texture = texturePaths[position]
                        updateSpinner(obj)
                    }
                }
            }
        }
    }

    private fun showOptions() {
        currentObject?.also { obj ->
            updateSpinner(obj)
            widthSeekBar.progress = (obj.orientation.scaleX / 0.5f).roundToInt() - 1
            widthValue.text = "W: ${obj.orientation.scaleX}"
            heightSeekBar.progress = (obj.orientation.scaleY / 0.5f).roundToInt() - 1
            heightValue.text = "H: ${obj.orientation.scaleY}"
            rotationSeekBar.progress = (obj.orientation.rotation / 5f).roundToInt()
            rotationSeekBar.visibility = View.VISIBLE
            rotationValue.text = "R: ${obj.orientation.rotation}"
            rotationValue.visibility = View.VISIBLE
            objectOptionsLayout.visibility = View.VISIBLE
        }
    }

    class EditorGameScene : GameScene {
        constructor(width: Float, height: Float, nodeRadius: Float) : super(width, height, nodeRadius)
        constructor(sceneMap: SceneMap) : super(sceneMap)

        private val debugTexture = OpenGLRenderer.getTexture("debug_selection.png")
        init {
            isEditor = true
            drawShadows = false
        }
        val current = ColorSprite(1f, 0f, 0f, 0.4f)

        var currentEditObject: GameObject? = null
        override fun draw(renderer: OpenGLRenderer) {
            super.draw(renderer)
            currentEditObject?.also {
                renderer.mvpMatrixWithCamera().also { mvpMatrix ->
                    val p = ((System.currentTimeMillis() % 2000) / 1000f) * PI
                    val t = sin(p).toFloat()
                    debugTexture.bind()
                    current.orientation.pos.x = it.orientation.pos.x
                    current.orientation.pos.y = it.orientation.pos.y
                    current.orientation.scaleX = it.orientation.scaleX + 0.1f *t
                    current.orientation.scaleY = it.orientation.scaleY + 0.1f *t
                    current.orientation.rotation = it.orientation.rotation
                    current.draw(mvpMatrix)
                }
            }
        }
    }
}
