package com.raywenderlich.listmaker

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.raywenderlich.listmaker.databinding.MainActivityBinding
import com.raywenderlich.listmaker.ui.detail.ListDetailActivity
import com.raywenderlich.listmaker.ui.detail.ui.detail.ListDetailFragment
import com.raywenderlich.listmaker.ui.main.MainFragment
import com.raywenderlich.listmaker.ui.main.MainViewModel
import com.raywenderlich.listmaker.ui.main.MainViewModelFactory

class MainActivity : AppCompatActivity(),
    MainFragment.MainFragmentInteractionListener {

    // variable to store the binding
    private lateinit var binding: MainActivityBinding

    // Variable to hold the ViewModel
    private lateinit var viewModel: com.raywenderlich.listmaker.ui.main.MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this, MainViewModelFactory(PreferenceManager.getDefaultSharedPreferences(this))
        ).get(MainViewModel::class.java)

        binding = MainActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (savedInstanceState == null) {
            // 1
            val mainFragment = MainFragment.newInstance()
            mainFragment.clickListener = this
            // 2
            val fragmentContainerViewId: Int = if (binding.mainFragmentContainer == null) {
                R.id.detail_container
            } else {
                R.id.main_fragment_container
            }
            // 3
            supportFragmentManager.commit { setReorderingAllowed(true)
                add(fragmentContainerViewId, mainFragment)
            }
        }
    }

    override fun listItemTapped(list: TaskList) {
        showListDetail(list)
    }

    private fun showCreateListDialog() {
        // 1
        val dialogTitle = getString(R.string.name_of_list)
        val positiveButtonTitle = getString(R.string.create_list)

        // 2
        val builder = AlertDialog.Builder(this)
        val listTitleEditText = EditText(this)
        listTitleEditText.inputType = InputType.TYPE_CLASS_TEXT

        builder.setTitle(dialogTitle)
        builder.setView(listTitleEditText)

        // 3
        builder.setPositiveButton(positiveButtonTitle) { dialog, _ -> dialog.dismiss()

        val taskList = TaskList(listTitleEditText.text.toString())
        viewModel.saveList(taskList)
        showListDetail(taskList)
        }

        // 4
        builder.create().show()
    }

    private fun showListDetail(list: TaskList) {
        if (binding.mainFragmentContainer == null) {
            val listDetailIntent = Intent(
                this,
                ListDetailActivity::class.java
            )
            listDetailIntent.putExtra(INTENT_LIST_KEY, list)
            startActivityForResult(
                listDetailIntent,
                LIST_DETAIL_REQUEST_CODE
            )
        } else {
            val bundle = bundleOf(INTENT_LIST_KEY to list)
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(
                    R.id.list_detail_fragment_container,
                    ListDetailFragment::class.java, bundle, null
                )
            }
            binding.fabButton.setOnClickListener {
                showCreateTaskDialog()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 1
        if (requestCode == LIST_DETAIL_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // 2
            data?.let {
                // 3
                viewModel.updateList(data.getParcelableExtra(INTENT_LIST_KEY)!!)
                    viewModel.refreshLists()
            }
        }
    }

    private fun showCreateTaskDialog() {
        val taskEditText = EditText(this)
        taskEditText.inputType = InputType.TYPE_CLASS_TEXT
        AlertDialog.Builder(this)
            .setTitle(R.string.task_to_add)
            .setView(taskEditText)
            .setPositiveButton(R.string.add_task) { dialog, _ ->
                val task = taskEditText.text.toString()
                viewModel.addTask(task)
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onBackPressed() {
        // 1
        val listDetailFragment =
            supportFragmentManager.findFragmentById(R.id.list_detail_fragment_container)
        // 2
        if (listDetailFragment == null) {
            super.onBackPressed()
        } else {
            // 3
            title = resources.getString(R.string.app_name)
            // 4
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                remove(listDetailFragment)
            }
            // 5
            binding.fabButton.setOnClickListener {
                showCreateListDialog()
            }
        }
    }

    companion object {
        const val INTENT_LIST_KEY = "list"
        const val LIST_DETAIL_REQUEST_CODE = 123
    }
}
