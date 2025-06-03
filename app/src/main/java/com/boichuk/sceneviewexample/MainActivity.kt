package com.boichuk.sceneviewexample

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.boichuk.sceneviewexample.databinding.ActivityMainBinding
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var sceneView: SceneView
    private lateinit var loadingView: View
    private lateinit var binding: ActivityMainBinding

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
            val hdrFile = "environments/studio_small_09_2k.hdr"
            sceneView.environmentLoader.loadHDREnvironment(hdrFile).apply {
                sceneView.indirectLight = this?.indirectLight
                sceneView.skybox = this?.skybox
            }
            sceneView.cameraNode.apply {
//                position = Position(/*z = 4.0f*/ x = .5f, y = .5f)
            }

            val modelInstance = sceneView.modelLoader.createModelInstance("models/Schwimmhalle.glb")
            val modelNode = ModelNode(
                modelInstance = modelInstance,
                scaleToUnits = 2.0f,
                centerOrigin = Position(-.5f, -.5f, .5f)
            )
            modelNode.scale = Scale(0.04f)
            sceneView.addChildNode(modelNode)
            loadingView.isGone = true
        }
    }
}