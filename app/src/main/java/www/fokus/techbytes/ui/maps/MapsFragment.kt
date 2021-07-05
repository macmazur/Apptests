package www.fokus.techbytes.ui.maps

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.array
import com.beust.klaxon.get
import com.beust.klaxon.obj
import com.beust.klaxon.string
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PatternItem
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import www.fokus.techbytes.R
import www.fokus.techbytes.databinding.FragmentMapsBinding
import www.fokus.techbytes.ui.about.AboutViewModel
import www.fokus.techbytes.ui.base.BaseFragment
import www.fokus.techbytes.utils.screenRectDp
import java.io.InputStream
import java.util.*
import kotlin.math.roundToInt


class MapsFragment : BaseFragment<FragmentMapsBinding, AboutViewModel>()
    ,OnMapReadyCallback ,GoogleMap.OnPolylineClickListener {

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val AUTOCOMPLETE_REQUEST_CODE = 10
        private const val SPEACH_REQUEST_CODE = 11
    }

    private val TAG = MapsFragment::class.java.simpleName
    override val viewModel: AboutViewModel by activityViewModels()

    /*private val REQUEST_LOCATION_PERMISSION = 1
    private val AUTOCOMPLETE_REQUEST_CODE = 10
    private val SPEACH_REQUEST_CODE = 11*/

    private lateinit var sheetBehavior: BottomSheetBehavior<LinearLayout>
    //private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private var polylineList = mutableListOf<Polyline>()
    val PATTERN_DASH_LENGTH_PX = 20
    val PATTERN_GAP_LENGTH_PX = 20
    val DOT: PatternItem = Dot()
    val DASH: PatternItem = Dash(PATTERN_DASH_LENGTH_PX.toFloat())
    val GAP: PatternItem = Gap(PATTERN_GAP_LENGTH_PX.toFloat())
    val PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH)


    private fun checkLocationPermission(): Boolean {
        /*
        locationPermissions.forEach {
            if (ContextCompat.checkSelfPermission(context!!, it) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
         */
        return (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestLocationPermission() {
        // optional implementation of shouldShowRequestPermissionRationale
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(requireContext())
                .setMessage("Need location permission to get current place")
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    // ActivityCompat.requestPermissions(activity!!, locationPermissions, REQUEST_LOCATION_PERMISSION)
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
        else {
            // ActivityCompat.requestPermissions(activity!!, locationPermissions, REQUEST_LOCATION_PERMISSION)
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }

    private fun enableMyLocation() {
        if (checkLocationPermission()) {
            map.isMyLocationEnabled = true
            /*binding.map
            val myLocationButton: View = map.findViewWithTag("GoogleMapMyLocationButton")
            val rlp = myLocationButton.layoutParams as RelativeLayout.LayoutParams
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)*/
        }
        else {
            requestLocationPermission()
        }
    }

    /*private val onPollyClick = GoogleMap.OnPolylineClickListener {
        toast("Route type " + it.tag.toString())
    }*/

    override fun onPolylineClick(polyline: Polyline) {
        toast("Route type " + polyline.tag.toString())
        polyline.remove()
    }

    private lateinit var map: GoogleMap
    override fun onMapReady(googleMap: GoogleMap?) {
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
        map = googleMap!!
        setMapStyle(map)
        //val heightDp = (2 * screenRectDp.height()).roundToInt() - 140
        //map.setPadding(0, heightDp, 0, 0)
        val zoomLevel = 15f

        //remove polly


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
        //map.setOnPolylineClickListener { onPollyClick }
        map.setOnPolylineClickListener(this)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(kolska, zoomLevel))
        enableMyLocation()
        toast("map ready!")
    }
    /*
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
        //val heightDp = (2 * screenRectDp.height()).roundToInt() - 140
        //map.setPadding(0, heightDp, 0, 0)
        val zoomLevel = 15f

        //remove polly


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
        //map.setOnPolylineClickListener { onPollyClick }
        map.setOnPolylineClickListener(this)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(kolska, zoomLevel))
        enableMyLocation()
        toast("map ready!")
    }
    */
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
        mapFragment?.getMapAsync(this) //callback

        val locationButton = (mapFragment?.view?.findViewById<View> (Integer.parseInt("1"))?.parent as View)
            .findViewById<View>(Integer.parseInt("2")) as ImageView
        val rlp =  locationButton.getLayoutParams() as RelativeLayout.LayoutParams
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        rlp.setMargins(0, 0, 30, 130);
        //locationButton.setImageResource(R.drawable.ic_my_location_button)
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
            //getSpeechInput()
            val start = LatLng(52.25081, 20.97806)
            val destination = LatLng(54.77355, 18.46516)
            drawRoute(start, destination)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
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

    /*https://developers.google.com/maps/documentation/directions/get-directions*/
    private fun drawRoute(origin: LatLng, destination: LatLng) {
        map.addMarker(MarkerOptions().position(origin).title("Start"))
        map.addMarker(MarkerOptions().position(destination).title("Koniec"))

        val parser: Parser = Parser()
        //val stringBuilder: StringBuilder = StringBuilder()

        val in_s: InputStream = getResources().openRawResource(R.raw.routes_example)
        val b = ByteArray(in_s.available())
        in_s.read(b)
        val stringBuilder: StringBuilder = StringBuilder(String(b))

        val json: JsonObject = parser.parse(stringBuilder) as JsonObject
        // get to the correct element in JsonObject
        val routes = json.array<JsonObject>("routes")
        if (routes != null) {
            for (i in 0 until routes.size) {

                val LatLongB = LatLngBounds.Builder()

                // Declare polyline object and set up color and width
                val options = PolylineOptions()
                options.clickable(true)
                //options.color(Color.GREEN)
                val rnd = Random()
                options.color(Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)))
                options.width(16f) //7f
                options.pattern(PATTERN_POLYGON_ALPHA)


                //@Suppress("UNCHECKED_CAST")
                val points = routes["legs"]["steps"][i] as JsonArray<JsonObject> // 0 -> i
                // For every element in the JsonArray, decode the polyline string and pass all points to a List
                val polypts = points.flatMap { decodePoly(it.obj("polyline")?.string("points")!!) }
                // Add  points to polyline and bounds
                options.add(origin)
                LatLongB.include(origin)
                for (point in polypts) {
                    options.add(point)
                    LatLongB.include(point)
                }
                options.add(destination)
                LatLongB.include(destination)
                // build bounds
                //val bounds = LatLongB.build()

                // add polyline to the map
                val polyline = map.addPolyline(options)
                polyline.tag = "Nr: $i"
                polylineList.add(polyline)

                // show map with route centered
                if (i == routes.size - 1)
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(LatLongB.build(), 100))
            }
        }
        // show map with route centered
        //map.moveCamera(CameraUpdateFactory.newLatLngBounds(LatLongB.build(), 100))

        // build URL to call API
        /*val url = getURL(origin, destination)
        async {
            // Connect to URL, download content and convert into string asynchronously
            val result = URL(url).readText()
            uiThread {
                // When API call is done, create parser and convert into JsonObjec
                val parser: Parser = Parser()
                val stringBuilder: StringBuilder = StringBuilder(result)
                val json: JsonObject = parser.parse(stringBuilder) as JsonObject
                // get to the correct element in JsonObject
                val routes = json.array<JsonObject>("routes")

                //var bounds: LatLngBounds
                //start loop
                if (routes != null) {
                    for (i in 0 until 2) { //routes.size


                        //@Suppress("UNCHECKED_CAST")
                        val points = routes["legs"]["steps"][i] as JsonArray<JsonObject> // 0 -> i
                        // For every element in the JsonArray, decode the polyline string and pass all points to a List
                        val polypts = points.flatMap { decodePoly(it.obj("polyline")?.string("points")!!) }
                        // Add  points to polyline and bounds
                        options.add(origin)
                        LatLongB.include(origin)
                        for (point in polypts) {
                            options.add(point)
                            LatLongB.include(point)
                        }
                        options.add(destination)
                        LatLongB.include(destination)
                        // build bounds
                        //val bounds = LatLongB.build()

                        // add polyline to the map
                        val pollyline = map.addPolyline(options)
                        pollyline.tag = "Nr: $i"
                    }
                }
                        //end loop

                        //wrap data!
                        *//*txtView1.text = routes["legs"]["start_address"][0].toString()
                            txtView2.text = routes["legs"]["end_address"][0].toString()
                            txtView3.text = routes["legs"]["distance"]["text"][0].toString() + " / " + routes["legs"]["duration"]["text"][0].toString()*//*

                        // show map with route centered
                        map.moveCamera(CameraUpdateFactory.newLatLngBounds(LatLongB.build(), 100)) //bounds
                    }
                }*/
        }

    /**
     * Method to decode polyline points
     */
    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5,
                lng.toDouble() / 1E5)
            poly.add(p)
        }

        return poly
    }

    private fun getURL(from : LatLng, to : LatLng) : String {
        val origin = "origin=" + from.latitude + "," + from.longitude
        val dest = "destination=" + to.latitude + "," + to.longitude
        val sensor = "sensor=false"
        val alternatives = "alternatives=true"
        val key = "key=" + getString(R.string.google_maps_key)
        //mode = driving default
        val params = "$origin&$dest&$sensor&$alternatives&$key"
        return "https://maps.googleapis.com/maps/api/directions/json?$params"
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentMapsBinding.inflate(inflater, container, false)
}
