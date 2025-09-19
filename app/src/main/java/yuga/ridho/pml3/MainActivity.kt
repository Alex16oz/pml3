package yuga.ridho.pml3

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import yuga.ridho.pml3.ui.theme.Pml3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Mengaktifkan persistensi data Firebase
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        setContent {
            Pml3Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MahasiswaScreen()
                }
            }
        }
    }
}

@Composable
fun MahasiswaScreen() {
    val context = LocalContext.current
    val db = FirebaseDatabase.getInstance().getReference("TabelMahasiswa")

    var nim by remember { mutableStateOf("") }
    var nama by remember { mutableStateOf("") }
    var alamat by remember { mutableStateOf("") }

    var mahasiswaList by remember { mutableStateOf<List<Mahasiswa>>(emptyList()) }

    // Listener untuk mengambil data dari Firebase
    LaunchedEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Mahasiswa>()
                snapshot.children.forEach {
                    it.getValue<Mahasiswa>()?.let { mhs -> list.add(mhs) }
                }
                mahasiswaList = list
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        db.addValueEventListener(listener)
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text("Data Mahasiswa", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nim,
            onValueChange = { nim = it },
            label = { Text("NIM") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = nama,
            onValueChange = { nama = it },
            label = { Text("Nama Mahasiswa") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = alamat,
            onValueChange = { alamat = it },
            label = { Text("Alamat") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Tombol-tombol aksi
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = {
                if (nim.isNotBlank()) {
                    val mahasiswa = Mahasiswa(nim, nama, alamat)
                    db.child(nim).setValue(mahasiswa)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Insert Berhasil", Toast.LENGTH_SHORT).show()
                            nim = ""
                            nama = ""
                            alamat = ""
                        }
                }
            }) {
                Text("Insert")
            }
            Button(onClick = {
                if (nim.isNotBlank()) {
                    val mahasiswa = Mahasiswa(nim, nama, alamat)
                    db.child(nim).setValue(mahasiswa)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Update Berhasil", Toast.LENGTH_SHORT).show()
                        }
                }
            }) {
                Text("Update")
            }
            Button(onClick = {
                if (nim.isNotBlank()) {
                    db.child(nim).removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Delete Berhasil", Toast.LENGTH_SHORT).show()
                            nim = ""
                            nama = ""
                            alamat = ""
                        }
                }
            }) {
                Text("Delete")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Daftar Mahasiswa
        LazyColumn {
            items(mahasiswaList) { mhs ->
                MahasiswaItem(mahasiswa = mhs) {
                    nim = mhs.nim
                    nama = mhs.namaMhs
                    alamat = mhs.alamatMhs
                }
            }
        }
    }
}

@Composable
fun MahasiswaItem(mahasiswa: Mahasiswa, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "NIM: ${mahasiswa.nim}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Nama: ${mahasiswa.namaMhs}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Alamat: ${mahasiswa.alamatMhs}", style = MaterialTheme.typography.bodySmall)
        }
    }
}