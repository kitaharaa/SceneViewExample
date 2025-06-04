package com.boichuk.sceneviewexample

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.boichuk.sceneviewexample.databinding.ActivityMainBinding
import com.boichuk.sceneviewexample.databinding.LayoutModelInfoBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var sceneView: SceneView
    private lateinit var loadingView: View
    private lateinit var binding: ActivityMainBinding
    private val fileProcessing: GlbFileProcessing by lazy { GlbFileProcessing() }
    private lateinit var modelNode: ModelNode

    // We'll simulate pivot movement by shifting this position
    private var pivotOffset = Position(0f, 0.4f, 0f)
    private lateinit var dialog: BottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sceneView = binding.sceneView
        loadingView = binding.loadingView
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
//            TODO use to load background
//            val hdrFile = "environments/studio_small_09_2k.hdr"
//            sceneView.environmentLoader.loadHDREnvironment(hdrFile).apply {
//                sceneView.indirectLight = this?.indirectLight
//                sceneView.skybox = this?.skybox
//            }

            launch(Dispatchers.IO) { fileProcessing.getFileJson(this@MainActivity) }
            setupModel()
            loadingView.isGone = true
        }

        with(binding) {
            up.setOnClickListener { movePivot(BoxMovementAction.IN) }
            down.setOnClickListener { movePivot(BoxMovementAction.OUT) }
            left.setOnClickListener { movePivot(BoxMovementAction.LEFT) }
            right.setOnClickListener { movePivot(BoxMovementAction.RIGHT) }

            info.setOnClickListener { dialog.show() }
        }

        setupInfoBottomSheet()
    }

    private fun setupInfoBottomSheet() {
        val binding = LayoutModelInfoBottomSheetBinding.inflate(layoutInflater)
        dialog = BottomSheetDialog(this)
        dialog.setContentView(binding.root)
        val bottomSheet = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
//            behavior.peekHeight = 53  // <-- visible part before drag (in pixels)
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            behavior.isDraggable = true
        }
    }

    private fun setupModel() {
        val file = fileProcessing.getGlbFile(this@MainActivity)
        val modelInstance = sceneView.modelLoader.createModelInstance(file)
        modelNode = ModelNode(
            modelInstance = modelInstance,
        ).apply {
            scale = Scale(0.04f)
            position = -pivotOffset
        }

        sceneView.addChildNode(modelNode)
    }

    private fun movePivot(action: BoxMovementAction) {
        val step = 0.1f
        pivotOffset = when (action) {
            BoxMovementAction.UP -> pivotOffset.copy(y = pivotOffset.y + step)
            BoxMovementAction.DOWN -> pivotOffset.copy(y = pivotOffset.y - step)
            BoxMovementAction.LEFT -> pivotOffset.copy(x = pivotOffset.x - step)
            BoxMovementAction.RIGHT -> pivotOffset.copy(x = pivotOffset.x + step)
            BoxMovementAction.IN -> pivotOffset.copy(z = pivotOffset.z - step)
            BoxMovementAction.OUT -> pivotOffset.copy(z = pivotOffset.z + step)
        }

        modelNode.position = -pivotOffset
        Log.d("PivotOffset", "New simulated pivot: $pivotOffset")
    }

    enum class BoxMovementAction {
        UP, DOWN, LEFT, RIGHT, IN, OUT;
    }
}
