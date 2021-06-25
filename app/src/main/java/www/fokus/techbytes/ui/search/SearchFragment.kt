package www.fokus.techbytes.ui.search

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import dagger.hilt.android.AndroidEntryPoint
import www.fokus.techbytes.R
import www.fokus.techbytes.databinding.FragmentSearchBinding
import www.fokus.techbytes.ui.about.AboutViewModel
import www.fokus.techbytes.ui.base.BaseFragment


/**
 * A [Fragment] that displays search.
 */
@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding, AboutViewModel>() {

    private val TAG = SearchFragment::class.java.simpleName
    private val AUTOCOMPLETE_REQUEST_CODE = 1
    private val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

    override val viewModel: AboutViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        hideBottomBarFab()

        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext(), getString(R.string.google_maps_key))
        }

        // Places by Intent
        binding.fab.setOnClickListener {
            // Set the fields to specify which types of place data to
            // return after the user has made a selection.
            //val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

            // Start the autocomplete intent.
            /*val intent = context?.let { it1 ->
                Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(it1)
            }
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)*/
            getAutoComplete(null)
        }

        // Places by fragment
        // Initialize the AutocompleteSupportFragment.

        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?

        autocompleteFragment?.setPlaceFields(fields)

        autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.name + ", " + place.id)
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: $status")
            }
        })

        // Places programatically

        // Create a new Places client instance.
        val placesClient = Places.createClient(applicationContext())

        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        val token = AutocompleteSessionToken.newInstance()

        // Create a RectangularBounds object.
        //val kolska = LatLng(52.25081, 20.97806)
        val bounds = RectangularBounds.newInstance(
            LatLng(50.25081, 18.97806),
            LatLng(55.25081, 22.97806)
        )

        // Use the builder to create a FindAutocompletePredictionsRequest.
        val request =
            FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
                .setLocationBias(bounds)
                //.setLocationRestriction(bounds)
                .setOrigin(LatLng(52.25081, 20.97806))
                .setCountries("PL")
                .setTypeFilter(TypeFilter.ADDRESS)
                .setSessionToken(token)
                .setQuery("Chalupy 3")
                .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                for (prediction in response.autocompletePredictions) {
                    Log.i(TAG, prediction.placeId)
                    Log.i(TAG, prediction.getPrimaryText(null).toString())
                    binding.editQuery.text = prediction.getPrimaryText(null).toString()
                }
            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: " + exception.statusCode)
                }
            }

        // Speech recognizon
        binding.btnSpeak.setOnClickListener {
            getSpeechInput()
        }


        super.onViewCreated(view, savedInstanceState)
    }

    /**
     * Override the activity's onActivityResult(), check the request code, and
     * do something with the returned place data (in this example its place name and place ID).
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                Log.i(TAG, "Place: " + place.name + ", " + place.id)
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                val status = Autocomplete.getStatusFromIntent(data!!)
                Log.i(TAG, status.statusMessage.toString())
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }

        when (requestCode) {
            10 -> if (resultCode == RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                //binding.txvResult.text = result?.get(0) ?: "Nie rozpoznano mowy"

                if (result != null) {
                    val spechText = result.get(0)
                    binding.txvResult.text = spechText
                    getAutoComplete(spechText)
                }
            }
        }
    }

    /*private fun getAutoComplete(query: String?) {
        // Start the autocomplete intent.
        val intent = context?.let { it1 ->
            Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setInitialQuery(query)
                .build(it1)
        }
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }*/

    /*private fun getSpeechInput() {
        *//*val intent = context?.let { it1 ->
            Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(it1)
        }*//*

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault())

        if (intent.resolveActivity(applicationContext().packageManager) != null) {
            startActivityForResult(intent, 10)
        } else {
            toast("Your Device Doesn't Support Speech Input")
        }
    }*/


    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSearchBinding.inflate(inflater, container, false)

    companion object {
    }
}
