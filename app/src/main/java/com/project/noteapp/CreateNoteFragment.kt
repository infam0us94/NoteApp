package com.project.noteapp

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import com.project.noteapp.database.NotesDatabase
import com.project.noteapp.databinding.FragmentCreateNoteBinding
import com.project.noteapp.entities.Notes
import com.project.noteapp.util.NoteBottomSheetFragment
import kotlinx.android.synthetic.main.fragment_create_note.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CreateNoteFragment : BaseFragment() {

    private val TAG = "CreateNoteFragment"

    private var _binding: FragmentCreateNoteBinding? = null
    private val binding get() = _binding!!

    var selectedColor = "#171C26"
    var currentDate: String? = null
    private var REQUEST_CODE_IMAGE = 456
    private var selectedImagePath = ""
    private var webLink = ""
    private var noteId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteId = requireArguments().getInt("noteId", -1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =  FragmentCreateNoteBinding.inflate(inflater, container, false)

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            CreateNoteFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (noteId != -1) {
            launch {
                context?.let {
                    var notes = NotesDatabase.getDatabase(it).noteDao().getSpecificNote(noteId)
                    binding.colorView.setBackgroundColor(Color.parseColor(notes.color))
                    binding.etNoteTitle.setText(notes.title)
                    binding.etNoteSubTitle.setText(notes.subTitle)
                    binding.etNoteDesc.setText(notes.noteText)
                    if (notes.imgPath != "") {
                        selectedImagePath = notes.imgPath!!
                        binding.imgNote.setImageBitmap(BitmapFactory.decodeFile(notes.imgPath))
                        binding.layoutImage.visibility = View.VISIBLE
                        binding.imgNote.visibility = View.VISIBLE
                        binding.imgDelete.visibility = View.VISIBLE
                    } else {
                        binding.layoutImage.visibility = View.GONE
                        binding.imgNote.visibility = View.GONE
                        binding.imgDelete.visibility = View.GONE
                    }

                    if (notes.webLink != "") {
                        webLink = notes.webLink!!
                        binding.tvWebLink.text = notes.webLink
                        binding.layoutWebUrl.visibility = View.VISIBLE
                        binding.etWebLink.setText(notes.webLink)
                        binding.imgUrlDelete.visibility = View.VISIBLE
                    } else {
                        binding.imgUrlDelete.visibility = View.GONE
                        binding.layoutWebUrl.visibility = View.GONE
                    }
                }
            }
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            BroadcastReceiver, IntentFilter("bottom_sheet_action")
        )

        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")

        currentDate = sdf.format(Date())
        binding.colorView.setBackgroundColor(Color.parseColor(selectedColor))

        binding.tvDateTime.text = currentDate

        binding.imgDone.setOnClickListener {
            if (noteId != -1) {
                updateNote()
            } else {
                saveNote()
            }
        }

        binding.imgBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.imgMore.setOnClickListener {
            var noteBottomSheetFragment = NoteBottomSheetFragment.newInstance(noteId)
            noteBottomSheetFragment.show(
                requireActivity().supportFragmentManager,
                "Note Bottom Sheet Fragment"
            )
        }

        binding.imgDelete.setOnClickListener {
            selectedImagePath = ""
            layoutImage.visibility = View.GONE
        }

        binding.btnOk.setOnClickListener {
            if (etWebLink.text.toString().trim().isNotEmpty()) {
                checkWebUrl()
            } else {
                Toast.makeText(requireContext(), "Url is Required", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCancel.setOnClickListener {
            if (noteId != -1) {
                tvWebLink.visibility = View.VISIBLE
                layoutWebUrl.visibility = View.GONE
            } else {
                layoutWebUrl.visibility = View.GONE
            }
        }

        binding.imgUrlDelete.setOnClickListener {
            webLink = ""
            tvWebLink.visibility = View.GONE
            imgUrlDelete.visibility = View.GONE
            layoutWebUrl.visibility = View.GONE
        }

        binding.tvWebLink.setOnClickListener {
            var intent = Intent(Intent.ACTION_VIEW, Uri.parse(etWebLink.text.toString()))
            startActivity(intent)
        }
    }


    private fun updateNote() {
        launch {

            context?.let {
                var notes = NotesDatabase.getDatabase(it).noteDao().getSpecificNote(noteId)

                notes.title = etNoteTitle.text.toString()
                notes.subTitle = etNoteSubTitle.text.toString()
                notes.noteText = etNoteDesc.text.toString()
                notes.dateTime = currentDate
                notes.color = selectedColor
                notes.imgPath = selectedImagePath
                notes.webLink = webLink

                NotesDatabase.getDatabase(it).noteDao().updateNote(notes)
                binding.etNoteTitle.setText("")
                binding.etNoteSubTitle.setText("")
                binding.etNoteDesc.setText("")
                binding.layoutImage.visibility = View.GONE
                binding.imgNote.visibility = View.GONE
                binding.tvWebLink.visibility = View.GONE
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    private fun saveNote() {

        if (binding.etNoteTitle.text.isNullOrEmpty()) {
            Toast.makeText(context, "Note Title is Required", Toast.LENGTH_SHORT).show()
        } else if (binding.etNoteSubTitle.text.isNullOrEmpty()) {

            Toast.makeText(context, "Note Sub Title is Required", Toast.LENGTH_SHORT).show()
        } else if (binding.etNoteDesc.text.isNullOrEmpty()) {

            Toast.makeText(context, "Note Description is Required", Toast.LENGTH_SHORT).show()
        } else {
            launch {
                var notes = Notes()
                notes.title = etNoteTitle.text.toString()
                notes.subTitle = etNoteSubTitle.text.toString()
                notes.noteText = etNoteDesc.text.toString()
                notes.dateTime = currentDate
                notes.color = selectedColor
                notes.imgPath = selectedImagePath
                notes.webLink = webLink
                context?.let {
                    NotesDatabase.getDatabase(it).noteDao().insertNotes(notes)
                    binding.etNoteTitle.setText("")
                    binding.etNoteSubTitle.setText("")
                    binding.etNoteDesc.setText("")
                    binding.layoutImage.visibility = View.GONE
                    binding.imgNote.visibility = View.GONE
                    binding.tvWebLink.visibility = View.GONE
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun deleteNote() {
        launch {
            context?.let {
                NotesDatabase.getDatabase(it).noteDao().deleteSpecificNote(noteId)
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    private fun checkWebUrl() {
        if (Patterns.WEB_URL.matcher(etWebLink.text.toString()).matches()) {
            binding.layoutWebUrl.visibility = View.GONE
            binding.etWebLink.isEnabled = false
            webLink = etWebLink.text.toString()
            binding.tvWebLink.visibility = View.VISIBLE
            binding.tvWebLink.text = etWebLink.text.toString()
        } else {
            Toast.makeText(requireContext(), "Url is not valid", Toast.LENGTH_SHORT).show()
        }
    }


    private val BroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {

            var actionColor = p1!!.getStringExtra("action")

            when (actionColor!!) {

                "Blue" -> {
                    selectedColor = p1.getStringExtra("selectedColor")!!
                    binding.colorView.setBackgroundColor(Color.parseColor(selectedColor))

                }

                "Yellow" -> {
                    selectedColor = p1.getStringExtra("selectedColor")!!
                    binding.colorView.setBackgroundColor(Color.parseColor(selectedColor))

                }


                "Purple" -> {
                    selectedColor = p1.getStringExtra("selectedColor")!!
                    binding.colorView.setBackgroundColor(Color.parseColor(selectedColor))

                }


                "Green" -> {
                    selectedColor = p1.getStringExtra("selectedColor")!!
                    binding.colorView.setBackgroundColor(Color.parseColor(selectedColor))

                }

                "Orange" -> {
                    selectedColor = p1.getStringExtra("selectedColor")!!
                    binding.colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }

                "Black" -> {
                    selectedColor = p1.getStringExtra("selectedColor")!!
                    binding.colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }

                "Image" -> {
                    readStorageTask()
                    binding.layoutWebUrl.visibility = View.GONE
                }

                "WebUrl" -> {
                    binding.layoutWebUrl.visibility = View.VISIBLE
                }

                "DeleteNote" -> {
                    deleteNote()
                }
                else -> {
                    binding.layoutImage.visibility = View.GONE
                    binding.imgNote.visibility = View.GONE
                    binding.layoutWebUrl.visibility = View.GONE
                    selectedColor = p1.getStringExtra("selectedColor")!!
                    binding.colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }
            }
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(BroadcastReceiver)
        super.onDestroy()
    }

    private fun readStorageTask() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            askForPermission()
        } else {
            openImageChooser()
        }
    }

    private fun getPathFromUri(contentUri: Uri): String? {
        var filePath: String? = null
        var cursor = requireActivity().contentResolver.query(contentUri, null, null, null, null)
        if (cursor == null) {
            filePath = contentUri.path
        } else {
            cursor.moveToFirst()
            var index = cursor.getColumnIndex("_data")
            filePath = cursor.getString(index)
            cursor.close()
        }
        return filePath
    }

    private fun openImageChooser() {
        Intent(Intent.ACTION_GET_CONTENT).also {
            it.type = "image/*"
            it.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            it.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(it, REQUEST_CODE_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                var selectedImageUrl = data.data
                if (selectedImageUrl != null) {
                    try {
                        var inputStream =
                            requireActivity().contentResolver.openInputStream(selectedImageUrl)
                        var bitmap = BitmapFactory.decodeStream(inputStream)
                        binding.imgNote.setImageBitmap(bitmap)
                        binding.imgNote.visibility = View.VISIBLE
                        binding.layoutImage.visibility = View.VISIBLE

                        selectedImagePath = getPathFromUri(selectedImageUrl)!!
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                        Log.w(TAG, e.message.toString())
                    }
                }
            }
        }
//        if (resultCode == AppCompatActivity.RESULT_OK) {
//            when (requestCode) {
//                REQUEST_CODE_IMAGE -> {
//                    val selectedImage = data?.data
//                    photoList.clear()
//                    photoList.add(selectedImage!!)
//                    imgNote.setImageURI(selectedImage)
//                    imgNote.visibility = View.VISIBLE
//                    layoutImage.visibility = View.VISIBLE
//                    selectedImagePath = selectedImage.toString()
//                }
//            }
//        }
    }

    private fun askForPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    "????????????????????, ???????????????????????? ???????????????????? ???????????? ?? ??????????????",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(
                    Html.fromHtml("<font color=\"#FF9800\">??????????????????</font>")
                ) { v: View? ->
                    ActivityCompat.requestPermissions(
                        requireActivity(), arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ),
                        REQUEST_CODE_IMAGE
                    )
                }.show()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    REQUEST_CODE_IMAGE
                )
            }
        } else {
            openImageChooser()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}