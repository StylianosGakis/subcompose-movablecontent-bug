package bug.movable_content_crash // ktlint-disable package-name

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.dp
import bug.movable_content_crash.ui.theme.MovablecontentcrashTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MovablecontentcrashTheme {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          Repro()
        }
      }
    }
  }
}

data class CtaButtonParams(
  val text: String,
  val onClick: () -> Unit,
  val modifier: Modifier = Modifier,
)

@Composable
fun Repro() {
  val ctaButton: @Composable (CtaButtonParams) -> Unit = remember {
    movableContentOf<CtaButtonParams> { ctaButtonParams ->
      MyButton(
        text = ctaButtonParams.text,
        onClick = ctaButtonParams.onClick,
        modifier = Modifier
          .then(ctaButtonParams.modifier)
          .border(1.dp, Color.Red),
      )
    }
  }
  var switch by remember { mutableStateOf(true) }
  Column(
    verticalArrangement = Arrangement.spacedBy(50.dp),
    modifier = Modifier.fillMaxSize(),
  ) {
    Button(onClick = { switch = !switch }) {
      Text(text = "Flip me")
    }
    Column {
      Text("Without subcompose, does work")
      WithoutSubcomposeLayout(ctaButton, switch)
    }
    Column {
      Text("With subcompose, doesn't work")
      WithSubcomposeLayout(ctaButton, switch)
    }
  }
}

@Composable
fun WithSubcomposeLayout(
  ctaButton: @Composable (CtaButtonParams) -> Unit,
  switch: Boolean,
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(50.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth(),
  ) {
    if (switch) {
      ctaButton(CtaButtonParams("1", onClick = { Log.d("TAG", "1") }, Modifier.padding(horizontal = 16.dp)))
    } else {
      SubcomposeLayout { constraints ->
        layout(constraints.maxWidth, constraints.maxHeight) {
          val cta: List<Placeable> = subcompose(Unit) {
            ctaButton(CtaButtonParams("2", onClick = { Log.d("TAG", "2") }, Modifier.padding(horizontal = 32.dp)))
          }.map { it.measure(constraints) }
          cta.first().place(0, 0)
        }
      }
    }
  }
}

@Composable
fun WithoutSubcomposeLayout(
  ctaButton: @Composable (CtaButtonParams) -> Unit,
  switch: Boolean,
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(50.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth(),
  ) {
    if (switch) {
      ctaButton(CtaButtonParams("1", onClick = { Log.d("TAG", "1") }, Modifier.padding(horizontal = 16.dp)))
    } else {
      ctaButton(CtaButtonParams("2", onClick = { Log.d("TAG", "2") }, Modifier.padding(horizontal = 32.dp)))
    }
  }
}

@Composable
fun MyButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier,
) {
  Button(onClick, modifier) {
    Text(text)
  }
}
