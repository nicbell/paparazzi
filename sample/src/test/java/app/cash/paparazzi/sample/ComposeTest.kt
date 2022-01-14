package app.cash.paparazzi.sample

import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class ComposeTest {
  @get:Rule
  val paparazzi = Paparazzi()

  @Test
  fun compose() {
    val view = ComposeView(paparazzi.context)
    val parent = FrameLayout(paparazzi.context).apply { id = android.R.id.content }
    parent.addView(view, MATCH_PARENT, MATCH_PARENT)
    PaparazziComposeOwner.register(parent)
    view.setContent {
      CircleSquare(modifier = Modifier
          .fillMaxSize()
          .padding(16.dp))
    }
    paparazzi.snapshot(parent)
  }
}

class PaparazziComposeOwner private constructor() : LifecycleOwner, SavedStateRegistryOwner {
  private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
  private val savedStateRegistryController = SavedStateRegistryController.create(this)

  override fun getLifecycle(): Lifecycle = lifecycleRegistry
  override fun getSavedStateRegistry(): SavedStateRegistry = savedStateRegistryController.savedStateRegistry

  companion object {
    fun register(view: View) {
      val owner = PaparazziComposeOwner()
      owner.savedStateRegistryController.performRestore(null)
      owner.lifecycleRegistry.currentState = Lifecycle.State.CREATED
      ViewTreeLifecycleOwner.set(view, owner)
      ViewTreeSavedStateRegistryOwner.set(view, owner)
    }
  }
}


@Composable
fun CircleSquare(modifier: Modifier = Modifier) {
  val animatedProgress = remember { Animatable(0f) }
  LaunchedEffect(animatedProgress) {
    animatedProgress.animateTo(
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
        ),
    )
  }

  val t = animatedProgress.value

  Canvas(modifier = modifier) {
    translate(size.width / 2f, size.height / 2f) {
      if (t <= 0.5) {
        val tt = map(t, 0f, 0.5f, 0f, 1f)
        val rotation = 90f * ease(tt, 3f)

        rotate(rotation, Offset(0f, 0f)) {
          drawCircles(270f, -360f * ease(tt, 3f))
        }
      } else {
        val tt = map(t, 0.5f, 1f, 0f, 1f)
        val rotation = -90f * ease(tt, 3f)

        rotate(rotation, Offset(0f, 0f)) {
          drawCircles(360f, 0f)
        }

        rotate(-rotation, Offset(0f, 0f)) {
          val rectSize = 2 * size.circleRadius()
          drawRect(
              color = Color.White,
              topLeft = Offset(-rectSize / 2f, -rectSize / 2f),
              size = Size(rectSize, rectSize),
          )
        }
      }
    }
  }
}

private fun DrawScope.drawCircles(sweepAngle: Float, rotation: Float) {
  val circleRadius = size.circleRadius()
  for (i in 0 until 4) {
    val r = circleRadius * sqrt(2f)
    val theta = (HALF_PI + PI * i) / 2f
    val tx = r * cos(theta)
    val ty = r * sin(theta)
    withTransform({
      translate(-tx, -ty)
      rotate(rotation, Offset(0f, 0f))
    }, {
      val rectSize = 2 * (circleRadius - circleRadius / 16f)
      drawArc(
          color = Color.Black,
          startAngle = 90f * (i + 1),
          sweepAngle = sweepAngle,
          useCenter = true,
          topLeft = Offset(-rectSize / 2f, -rectSize / 2f),
          size = Size(rectSize, rectSize),
      )
    })
  }
}

private fun Size.circleRadius(): Float {
  return min(width, height) / 4f / sqrt(2f)
}

private fun ease(p: Float, g: Float): Float {
  return if (p < 0.5f) {
    0.5f * pow(2 * p, g)
  } else {
    1 - 0.5f * pow(2 * (1 - p), g)
  }
}

private fun map(value: Float, start1: Float, stop1: Float, start2: Float, stop2: Float): Float {
  return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1))
}

private fun pow(n: Float, e: Float): Float {
  return n.pow(e)
}

private const val PI = Math.PI.toFloat()
private const val HALF_PI = PI / 2

@Composable
fun HelloWorld() {
  val text = "Hello, Paparazzi"
  Column(Modifier
      .background(Color.White)
      .fillMaxSize()
      .wrapContentSize()) {
    Text(text)
    Text(text, style = TextStyle(fontFamily = FontFamily.Cursive))
    Text(
        text = text,
        style = TextStyle(textDecoration = TextDecoration.LineThrough)
    )
    Text(
        text = text,
        style = TextStyle(textDecoration = TextDecoration.Underline)
    )
    Text(
        text = text,
        style = TextStyle(
            textDecoration = TextDecoration.combine(
                listOf(
                    TextDecoration.Underline,
                    TextDecoration.LineThrough
                )
            ),
            fontWeight = FontWeight.Bold
        )
    )
  }
}

@Composable
fun MySurface() {
  Box(Modifier
      .background(color = Color.White)
      .fillMaxSize()) {
    val popupWidth = 200.dp
    val popupHeight = 50.dp
    val cornerSize = 16.dp

    Box(
        Modifier
            .graphicsLayer {
              alpha = 0.5f
              shadowElevation = 5f
            }
            .size(popupWidth, popupHeight)
            .background(Color.White, RoundedCornerShape(cornerSize))
    )
  }
}

@Composable
fun MyPopup() {
  Box(Modifier
      .background(color = Color.Red)
      .fillMaxSize()) {
    val popupWidth = 200.dp
    val popupHeight = 50.dp
    val cornerSize = 16.dp

    Popup(alignment = Alignment.Center) {
      Box(
          Modifier
              .size(popupWidth, popupHeight)
              .background(Color.White, RoundedCornerShape(cornerSize))
      )
    }
  }
}

@Composable
fun MessageList() {
  Surface {
    val state = rememberLazyListState(initialFirstVisibleItemIndex = 14)
    LazyColumn(state = state) {
      items(50) { index ->
        Text(text = "Item: $index")
      }
    }
  }
}