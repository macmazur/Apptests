package www.fokus.techbytes.ui.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.util.*


abstract class BaseFragment<VB : ViewBinding, VM : ViewModel> : Fragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    private var fragmentListener: FragmentListener? = null

    protected abstract val viewModel: VM

    private val AUTOCOMPLETE_REQUEST_CODE = 1
    private val SPEACH_REQUEST_CODE = 10

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = getViewBinding(inflater, container)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.fragmentListener = context as? FragmentListener
    }

    protected abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    fun getAutoComplete(query: String?) {
        // Start the autocomplete intent.
        val intent = context?.let { it1 ->
            Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,
                listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
                .setInitialQuery(query)
                .build(it1)
        }
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    fun getSpeechInput() {

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault())

        if (intent.resolveActivity(applicationContext().packageManager) != null) {
            startActivityForResult(intent, SPEACH_REQUEST_CODE)
        } else {
            toast("Your Device Doesn't Support Speech Input")
        }
    }

    fun hideBottomBarFab() {
        fragmentListener?.hideBottomBarFab()
    }

    fun showBottomBarFab() {
        fragmentListener?.showBottomBarFab()
    }

    fun toast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    fun applicationContext(): Context = requireActivity().applicationContext

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
