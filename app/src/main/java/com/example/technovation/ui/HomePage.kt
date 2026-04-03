import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.technovation.ui.AllJournalEntries
import com.example.technovation.ui.AlreadyMadeEntryDialogue
import com.example.technovation.ui.AppPages
import com.example.technovation.ui.SymptomsViewModel
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Home(
    modifier: Modifier = Modifier,
    navController: NavController,
    allEntriesViewModel: AllJournalEntries,
    symptomsViewModel: SymptomsViewModel = viewModel()) {
    var showDialog by remember {mutableStateOf(false)}
    if (showDialog) {
        AlreadyMadeEntryDialogue(onDismiss = {showDialog= false})
    }
    Column(
        modifier=modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            //Need to get information from the login once done
            "Good afternoon, NAME",
            fontSize = 30.sp,
            modifier=Modifier
                .align(Alignment.CenterHorizontally)
                .padding(0.dp, 15.dp)
        )

        Spacer(modifier=Modifier.height(40.dp))

        Card(
            onClick = {
                if (allEntriesViewModel.hasEntryForDay(LocalDate.now())) {
                    showDialog = true
                } else {
                    symptomsViewModel.resetSelections()
                    navController.navigate(route = AppPages.NewEntry.title)
                }
            },
            modifier = Modifier
                .height(60.dp)
                .width(350.dp),
        ) {
            Text("Log your symptoms", fontSize=20.sp)
        }

        Spacer(modifier=Modifier.height(20.dp))

        Card(
            onClick = {},
            modifier = Modifier
                .height(60.dp)
                .width(350.dp),
        ) {
            Text("Your next medication", fontSize=20.sp)
        }

        Spacer(modifier=Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .height(60.dp)
                .width(350.dp),
        ) {
            Text("Article", fontSize=20.sp)
        }
    }
}