package www.fokus.techbytes.ui.maps

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.gms.common.api.Status

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import www.fokus.techbytes.R
import www.fokus.techbytes.databinding.FragmentMapsBinding
import www.fokus.techbytes.ui.about.AboutViewModel
import www.fokus.techbytes.ui.base.BaseFragment

class MapsFragment : BaseFragment<FragmentMapsBinding, AboutViewModel>() {

    private val TAG = MapsFragment::class.java.simpleName
    override val viewModel: AboutViewModel by activityViewModels()
    private val AUTOCOMPLETE_REQUEST_CODE = 1
    private val SPEACH_REQUEST_CODE = 10
    private lateinit var sheetBehavior: BottomSheetBehavior<LinearLayout>
    //private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private lateinit var map: GoogleMap
    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Set the map style
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         * zoomLevel
            1: World
            5: Landmass/continent
            10: City
            15: Streets
            20: Buildings
         */
        map = googleMap
        setMapStyle(map)
        val zoomLevel = 15f

        /*markerPerth = map.addMarker(MarkerOptions().position(PERTH).title("Perth").icon(
            vectorToBitmap(R.drawable.ic_car_battery, Color.BLACK))) //Color.parseColor("#0066ff")
        markerPerth.tag = 0*/

        val kolska = LatLng(52.25081, 20.97806)
        val markerKolska = map.addMarker(MarkerOptions().position(kolska).title("Kolska"))
        markerKolska.tag = 0
        val pokorna = LatLng(52.25432, 20.99344)
        map.addMarker(MarkerOptions().position(pokorna).title("Pokorna"))

        map.setOnMarkerClickListener { marker ->
            //swipeToRefreshMap(true)
            // Retrieve the data from the marker.
            val clickCount = marker.tag as? Int

            // Check if a click count was set, then display the click count.
            clickCount?.let {
                val newClickCount = it + 1
                marker.tag = newClickCount
                toast("${marker.title} has been clicked $newClickCount times.")
            }
            sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            /*if (marker.isInfoWindowShown) {
                marker.hideInfoWindow()
            } else {
                marker.showInfoWindow()
            }*/
            true
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(kolska, zoomLevel))
    }

    /*mmazur
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //swipeToRefreshMap()
        showBottomBarFab()

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        //setup()
        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext(), getString(R.string.google_maps_key))
        }
        binding.tapSearch.setOnClickListener {
            // Set the fields to specify which types of place data to
            // return after the user has made a selection.
            //val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
            /*startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)*/
            getAutoComplete(null)
        }
        binding.tapSpeak.setOnClickListener {
            getSpeechInput()
        }

        sheetBehavior = BottomSheetBehavior.from<LinearLayout>(binding.bottomSheet.bottomSheetLayout)
        /**
         * bottom sheet state change listener
         * we are changing button text when sheet changed state
         * */
        sheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }
                    BottomSheetBehavior.STATE_EXPANDED ->
                        binding.bottomSheet.sheetTittle.text = "Close Bottom Sheet"
                    BottomSheetBehavior.STATE_COLLAPSED ->
                        binding.bottomSheet.sheetTittle.text = "Expand Bottom Sheet"
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        TODO()
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })

        sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

    }

    /**
     * Override the activity's onActivityResult(), check the request code, and
     * do something with the returned place data (in this example its place name and place ID).
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            AUTOCOMPLETE_REQUEST_CODE ->
                if (resultCode == Activity.RESULT_OK) {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(it)
                        binding.tapSearch.text = place.name
                        map.addMarker(place.latLng?.let {
                            MarkerOptions().position(it).title(place.name)
                        })
                        map.moveCamera(CameraUpdateFactory.newLatLng(place.latLng))
                    }
                } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                    // TODO: Handle the error.
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    Log.i(TAG, status.statusMessage.toString())
                } /*else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }*/
            SPEACH_REQUEST_CODE ->
                if (resultCode == Activity.RESULT_OK) {
                    val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val spechText = result?.get(0)
                    getAutoComplete(spechText)
                }
        }

        /*
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    val place = Autocomplete.getPlaceFromIntent(it)
                    binding.tapSearch.text = place.name
                    map.addMarker(place.latLng?.let {
                        MarkerOptions().position(it).title(place.name)
                    })
                    map.moveCamera(CameraUpdateFactory.newLatLng(place.latLng))
                }
                /*val place = Autocomplete.getPlaceFromIntent(data!!)
                binding.tapSearch.text = place.name
                //Log.i(TAG, "Place: " + place.name + ", " + place.id)
                map.addMarker(place.latLng?.let {
                    MarkerOptions().position(it).title(place.name)
                })
                map.moveCamera(CameraUpdateFactory.newLatLng(place.latLng))*/
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                val status = Autocomplete.getStatusFromIntent(data!!)
                Log.i(TAG, status.statusMessage.toString())
            } /*else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }*/
        }*/
    }

/*    private fun setup() = with(binding) {
        swipeToRefreshMap()
    }*/

    /*private fun swipeToRefreshMap(refresh : Boolean) = with(binding) {
        refreshMaps.setOnRefreshListener {
            if (refresh) {
                refreshMaps.setProgressViewOffset(false, 100, 100)
                refreshMaps.isRefreshing = true
            }
            else
                refreshMaps.isRefreshing = false
        }
    }*/

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentMapsBinding.inflate(inflater, container, false)
}
