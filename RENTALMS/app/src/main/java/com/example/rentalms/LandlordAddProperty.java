package com.example.rentalms;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.UUID;

public class LandlordAddProperty extends AppCompatActivity {

    private Button addPropertyButton, uploadRoomInteriorButton, uploadRoomExteriorButton;
    private EditText propertyNameEditText, propertyPriceEditText, barangayEditText, addressEditText;
    private Spinner propertyTypeSpinner, provinceSpinner, citySpinner, paymentPeriodSpinner;
    private ImageView roomInteriorImageView, roomExteriorImageView;

    private FirebaseFirestore db;  // Firestore instance
    private String landlordId;     // Landlord ID passed from login
    private Uri interiorImageUri = null, exteriorImageUri = null; // URIs to store selected images

    // Mapping provinces to their cities
    private HashMap<String, String[]> provinceCityMap;
    private boolean isUploadingInterior = false;

    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> choosePictureLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landlord_add_property);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve the landlord's unique ID from the previous activity
        landlordId = getIntent().getStringExtra("landlordId");

        // Initialize views
        addPropertyButton = findViewById(R.id.btn_add_property);
        propertyNameEditText = findViewById(R.id.property_name);
        propertyPriceEditText = findViewById(R.id.property_price);
        paymentPeriodSpinner = findViewById(R.id.payment_period_spinner);
        barangayEditText = findViewById(R.id.barangay); // Add new Barangay EditText
        addressEditText = findViewById(R.id.address);   // Add new Address EditText
        propertyTypeSpinner = findViewById(R.id.property_type_spinner);
        provinceSpinner = findViewById(R.id.province_spinner);
        citySpinner = findViewById(R.id.city_spinner);
        uploadRoomInteriorButton = findViewById(R.id.btn_upload_room_interior);
        uploadRoomExteriorButton = findViewById(R.id.btn_upload_bathroom_shower);
        roomInteriorImageView = findViewById(R.id.img_room_interior);
        roomExteriorImageView = findViewById(R.id.img_bathroom_shower);

        // Disable transformation method for the price edit text
        propertyPriceEditText.setTransformationMethod(null);

        // Limit input in propertyPriceEditText to 6 digits and format with commas
        propertyPriceEditText.addTextChangedListener(new TextWatcher() {
            private String currentText = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(currentText)) {
                    propertyPriceEditText.removeTextChangedListener(this);

                    // Remove non-digit characters
                    String cleanString = s.toString().replaceAll("[^\\d]", "");

                    // Limit to 6 digits
                    if (cleanString.length() > 6) {
                        cleanString = cleanString.substring(0, 6);
                    }

                    // Parse the number and format it with commas
                    try {
                        long parsed = Long.parseLong(cleanString);
                        String formatted = NumberFormat.getInstance().format(parsed);
                        currentText = formatted;
                        propertyPriceEditText.setText(formatted);
                        propertyPriceEditText.setSelection(formatted.length()); // Set cursor to end
                    } catch (NumberFormatException e) {
                        Log.e("Formatting", "Number format error: " + e.getMessage());
                    }

                    propertyPriceEditText.addTextChangedListener(this);
                }
            }
        });

        // Set up permission launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean storageGranted = result.getOrDefault(Manifest.permission.READ_MEDIA_IMAGES, false);
                    if (storageGranted != null && storageGranted) {
                        Log.d("Permissions", "Storage permission granted.");
                        choosePictureLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
                    } else {
                        Log.d("Permissions", "Storage permission denied.");
                        Toast.makeText(LandlordAddProperty.this, "Permission is required to access the gallery", Toast.LENGTH_SHORT).show();
                    }
                });

        choosePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            Uri selectedImageUri = data.getData();
                            Log.d("ImagePicker", "Selected Image URI: " + selectedImageUri);
                            if (isUploadingInterior) {
                                interiorImageUri = selectedImageUri;
                                roomInteriorImageView.setImageURI(interiorImageUri);
                                Log.d("ImagePicker", "Interior image URI: " + interiorImageUri);
                            } else {
                                exteriorImageUri = selectedImageUri;
                                roomExteriorImageView.setImageURI(exteriorImageUri);
                                Log.d("ImagePicker", "Exterior image URI: " + exteriorImageUri);
                            }
                        } else {
                            Log.e("ImagePicker", "No data found in intent.");
                        }
                    } else {
                        Log.e("ImagePicker", "Image selection failed with result code: " + result.getResultCode());
                    }
                });

        // Set up spinner for property types
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.property_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        propertyTypeSpinner.setAdapter(adapter);

        // Set up spinner for payment periods
        ArrayAdapter<CharSequence> paymentPeriodAdapter = ArrayAdapter.createFromResource(this,
                R.array.payment_period, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentPeriodSpinner.setAdapter(paymentPeriodAdapter);

        // Set up spinner for provinces
        ArrayAdapter<CharSequence> provinceAdapter = ArrayAdapter.createFromResource(this,
                R.array.provinces, android.R.layout.simple_spinner_item);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        provinceSpinner.setAdapter(provinceAdapter);

        // Set up the province-city mapping
        setupProvinceCityMap();

        // Change city options based on selected province
        provinceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedProvince = parent.getItemAtPosition(position).toString();
                loadCitiesForProvince(selectedProvince);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Add new property when the button is clicked
        addPropertyButton.setOnClickListener(v -> addPropertyToFirestore());

        uploadRoomInteriorButton.setOnClickListener(v -> {
            isUploadingInterior = true; // Set this to true when interior button is clicked
            checkAndRequestPermission("interior");
        });

        uploadRoomExteriorButton.setOnClickListener(v -> {
            isUploadingInterior = false; // Set this to false when exterior button is clicked
            checkAndRequestPermission("exterior");
        });
    }

    // Method to add property to Firestore
    private void addPropertyToFirestore() {
        // Get the property details
        String propertyName = propertyNameEditText.getText().toString().trim();
        String propertyPrice = propertyPriceEditText.getText().toString().trim();
        String propertyType = propertyTypeSpinner.getSelectedItem().toString();
        String propertyProvince = provinceSpinner.getSelectedItem().toString();
        String propertyCity = citySpinner.getSelectedItem().toString();
        String barangay = barangayEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String paymentPeriod = paymentPeriodSpinner.getSelectedItem().toString(); // Get payment period

        // Validate the input
        if (TextUtils.isEmpty(propertyName) || TextUtils.isEmpty(propertyPrice) ||
                propertyProvince.equals("Select a province") || propertyCity.equals("Select a city") ||
                TextUtils.isEmpty(barangay) || TextUtils.isEmpty(address)) {
            Toast.makeText(LandlordAddProperty.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format price with "₱" symbol
        String formattedPrice = "₱" + propertyPrice;

        // Prepare the property data to be saved in Firestore
        HashMap<String, Object> propertyData = new HashMap<>();
        propertyData.put("propertyName", propertyName);
        propertyData.put("price", formattedPrice);
        propertyData.put("type", propertyType);
        propertyData.put("province", propertyProvince);
        propertyData.put("city", propertyCity);
        propertyData.put("barangay", barangay + ",");
        propertyData.put("address", address + ",");
        propertyData.put("paymentPeriod", paymentPeriod); // Add payment period to property data

        // Track the number of completed uploads
        final int[] uploadCounter = {0};

        // Upload interior image if available
        if (interiorImageUri != null) {
            uploadImageToFirebase(interiorImageUri, "interior", propertyData, uploadCounter);
        } else {
            propertyData.put("interiorImageUrl", ""); // No interior image
            uploadCounter[0]++; // Increment counter for no image
        }

        // Upload exterior image if available
        if (exteriorImageUri != null) {
            uploadImageToFirebase(exteriorImageUri, "exterior", propertyData, uploadCounter);
        } else {
            propertyData.put("exteriorImageUrl", ""); // No exterior image
            uploadCounter[0]++; // Increment counter for no image
        }

        // Check if both uploads (if any) are done
        if (uploadCounter[0] == 2) {
            savePropertyToFirestore(propertyData);
        }
    }


    // Method to upload an image to Firebase Storage and update the property data
    private void uploadImageToFirebase(Uri imageUri, String type, HashMap<String, Object> propertyData, int[] uploadCounter) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("property_images/" + UUID.randomUUID().toString());
        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            if (type.equals("interior")) {
                                propertyData.put("interiorImageUrl", uri.toString());
                            } else {
                                propertyData.put("exteriorImageUrl", uri.toString());
                            }
                            // Increment the upload counter
                            uploadCounter[0]++;
                            // Check if both uploads are done
                            if (uploadCounter[0] == 2) {
                                savePropertyToFirestore(propertyData);
                            }
                        }))
                .addOnFailureListener(e -> Log.e("UploadImage", "Failed to upload image: " + e.getMessage()));
    }

    // Method to save property to Firestore
    private void savePropertyToFirestore(HashMap<String, Object> propertyData) {
        DocumentReference landlordDocRef = db.collection("Landlords").document(landlordId)
                .collection("properties").document();

        landlordDocRef.set(propertyData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(LandlordAddProperty.this, "Property added successfully!", Toast.LENGTH_SHORT).show();
                    clearInputFields();  // Clear the form
                    Intent intent = new Intent(LandlordAddProperty.this, LandlordPage.class);
                    intent.putExtra("landlordId", landlordId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LandlordAddProperty.this, "Failed to add property: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method to clear the input fields after adding a property
    private void clearInputFields() {
        propertyNameEditText.setText("");
        propertyPriceEditText.setText("");
        paymentPeriodSpinner.setSelection(0); // Reset to first item
        barangayEditText.setText("");  // Clear Barangay
        addressEditText.setText("");   // Clear Address
        propertyTypeSpinner.setSelection(0); // Reset to first item
        provinceSpinner.setSelection(0); // Reset to first item
        citySpinner.setSelection(0); // Reset to first item
        roomInteriorImageView.setImageResource(android.R.color.transparent); // Clear the image
        roomExteriorImageView.setImageResource(android.R.color.transparent); // Clear the image
        interiorImageUri = null;
        exteriorImageUri = null; // Reset image URIs
    }
    private void checkAndRequestPermission(String imageType) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13 and above, check specific media permissions
            if (ContextCompat.checkSelfPermission(LandlordAddProperty.this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed to select image
                choosePictureLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
            } else {
                // Request permission for Android 13 and above
                requestPermissionLauncher.launch(new String[]{Manifest.permission.READ_MEDIA_IMAGES});
            }
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Android 6.0 to 12.0, check READ_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(LandlordAddProperty.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed to select image
                choosePictureLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
            } else {
                // Request permission for Android 6.0 to 12.0
                requestPermissionLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
            }
        } else {
            // For versions lower than Android 6.0, proceed directly to select image
            choosePictureLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        }
    }



    // Set up province-city map
    private void setupProvinceCityMap() {
        provinceCityMap = new HashMap<>();

        // Existing provinces and cities...
        provinceCityMap.put("Abra", new String[]{"Bangued", "Boliney", "Bucay", "Bucloc", "Daguioman"});
        provinceCityMap.put("Agusan del Norte", new String[]{"Butuan", "Cabadbaran", "Buenavista", "Carmen", "Jabonga"});
        provinceCityMap.put("Agusan del Sur", new String[]{"Bayugan", "Bunawan", "Esperanza", "La Paz", "Loreto"});
        provinceCityMap.put("Aklan", new String[]{"Kalibo", "Banga", "Ibajay", "Numancia", "Altavas"});
        provinceCityMap.put("Albay", new String[]{"Legazpi", "Ligao", "Tabaco", "Daraga", "Malilipot"});
        provinceCityMap.put("Antique", new String[]{"San Jose de Buenavista", "Hamtic", "Sibalom", "Tibiao", "Caluya"});
        provinceCityMap.put("Apayao", new String[]{"Kabugao", "Conner", "Flora", "Luna", "Pudtol"});
        provinceCityMap.put("Aurora", new String[]{"Baler", "Casiguran", "Dilasag", "Dinalungan", "Dingalan"});
        provinceCityMap.put("Basilan", new String[]{"Isabela City", "Lamitan", "Maluso", "Sumisip", "Tipo-Tipo"});
        provinceCityMap.put("Bataan", new String[]{"Balanga", "Abucay", "Bagac", "Dinalupihan", "Mariveles"});
        provinceCityMap.put("Batanes", new String[]{"Basco", "Itbayat", "Ivana", "Mahatao", "Sabtang"});
        provinceCityMap.put("Batangas", new String[]{"Batangas City", "Lipa", "Tanauan", "Bauan", "Lemery"});
        provinceCityMap.put("Benguet", new String[]{"Baguio", "La Trinidad", "Itogon", "Mankayan", "Tuba"});
        provinceCityMap.put("Biliran", new String[]{"Naval", "Almeria", "Biliran", "Cabucgayan", "Caibiran"});
        provinceCityMap.put("Bohol", new String[]{"Tagbilaran", "Anda", "Carmen", "Dauis", "Panglao"});
        provinceCityMap.put("Bukidnon", new String[]{"Malaybalay", "Valencia", "Don Carlos", "Damulog", "Manolo Fortich"});
        provinceCityMap.put("Bulacan", new String[]{"Malolos", "Meycauayan", "San Jose del Monte", "Baliuag", "Marilao"});
        provinceCityMap.put("Cagayan", new String[]{"Tuguegarao", "Aparri", "Baggao", "Gonzaga", "Lal-lo"});
        provinceCityMap.put("Camarines Norte", new String[]{"Daet", "Basud", "Mercedes", "Labo", "Talisay"});
        provinceCityMap.put("Camarines Sur", new String[]{"Naga", "Iriga", "Pili", "Caramoan", "Goa"});
        provinceCityMap.put("Camiguin", new String[]{"Mambajao", "Catarman", "Guinsiliban", "Mahinog", "Sagay"});
        provinceCityMap.put("Capiz", new String[]{"Roxas City", "Panay", "Pilar", "Sigma", "Tapaz"});
        provinceCityMap.put("Catanduanes", new String[]{"Virac", "Bato", "Baras", "Bagamanoc", "San Andres"});
        provinceCityMap.put("Cavite", new String[]{"Trece Martires", "Dasmariñas", "Tagaytay", "Bacoor", "Imus"});
        provinceCityMap.put("Cebu", new String[]{"Cebu City", "Mandaue", "Lapu-Lapu", "Danao", "Toledo"});
        provinceCityMap.put("Compostela Valley", new String[]{"Nabunturan", "Compostela", "Monkayo", "Mawab", "Pantukan"});
        provinceCityMap.put("Cotabato", new String[]{"Kidapawan", "Matalam", "Midsayap", "Makilala", "Pikit"});
        provinceCityMap.put("Davao del Norte", new String[]{"Tagum", "Panabo", "Samal", "Carmen", "Kapalong"});
        provinceCityMap.put("Davao del Sur", new String[]{"Davao City", "Digos", "Bansalan", "Hagonoy", "Magsaysay"});
        provinceCityMap.put("Davao Oriental", new String[]{"Mati", "Baganga", "Cateel", "Governor Generoso", "Lupon"});
        provinceCityMap.put("Dinagat Islands", new String[]{"San Jose", "Basilisa", "Cagdianao", "Dinagat", "Libjo"});
        provinceCityMap.put("Eastern Samar", new String[]{"Borongan", "Balangiga", "Dolores", "Guiuan", "Llorente"});
        provinceCityMap.put("Guimaras", new String[]{"Jordan", "Buenavista", "Nueva Valencia", "San Lorenzo", "Sibunag"});
        provinceCityMap.put("Ifugao", new String[]{"Lagawe", "Aguinaldo", "Alfonso Lista", "Banaue", "Hungduan"});
        provinceCityMap.put("Ilocos Norte", new String[]{"Laoag", "Batac", "Badoc", "Bacarra", "Currimao"});
        provinceCityMap.put("Ilocos Sur", new String[]{"Vigan", "Candon", "Bantay", "Caoayan", "Narvacan"});
        provinceCityMap.put("Iloilo", new String[]{"Iloilo City", "Passi", "Oton", "Pototan", "Miagao"});
        provinceCityMap.put("Isabela", new String[]{"Ilagan", "Cauayan", "Santiago", "Tumauini", "Echague"});
        provinceCityMap.put("Kalinga", new String[]{"Tabuk", "Balbalan", "Lubuagan", "Pasil", "Tinglayan"});
        provinceCityMap.put("La Union", new String[]{"San Fernando", "Agoo", "Aringay", "Bacnotan", "Bauang"});
        provinceCityMap.put("Laguna", new String[]{"Santa Cruz", "Calamba", "Biñan", "San Pablo", "Santa Rosa"});
        provinceCityMap.put("Lanao del Norte", new String[]{"Iligan", "Tubod", "Kapatagan", "Sultan Naga Dimaporo", "Tangcal"});
        provinceCityMap.put("Lanao del Sur", new String[]{"Marawi", "Bacolod-Kalawi", "Balabagan", "Kapai", "Malabang"});
        provinceCityMap.put("Leyte", new String[]{"Tacloban", "Ormoc", "Baybay", "Abuyog", "Palo"});
        provinceCityMap.put("Maguindanao", new String[]{"Shariff Aguak", "Cotabato City", "Datu Odin Sinsuat", "Datu Saudi-Ampatuan", "Mamasapano"});
        provinceCityMap.put("Marinduque", new String[]{"Boac", "Gasan", "Mogpog", "Buenavista", "Torrijos"});
        provinceCityMap.put("Masbate", new String[]{"Masbate City", "Mobo", "Pio V. Corpus", "San Fernando", "Cawayan"});
        provinceCityMap.put("Misamis Occidental", new String[]{"Ozamiz City", "Tangub City", "El Salvador", "Plaridel", "Sinacaban"});
        provinceCityMap.put("Misamis Oriental", new String[]{"Cagayan de Oro", "Gingoog City", "El Salvador", "Initao", "Jasaan"});
        provinceCityMap.put("Mountain Province", new String[]{"Bontoc", "Sabangan", "Sagada", "Tadian", "Besao"});
        provinceCityMap.put("Negros Occidental", new String[]{"Bacolod City", "La Carlota", "Talisay", "Silay", "Victorias"});
        provinceCityMap.put("Negros Oriental", new String[]{"Dumaguete", "Bayawan", "Tanjay", "Canlaon", "Guihulngan"});
        provinceCityMap.put("Northern Samar", new String[]{"Catarman", "San Isidro", "Bobon", "Lapinig", "Las Navas"});
        provinceCityMap.put("Nueva Ecija", new String[]{"Palayan", "Cabanatuan", "Gapan", "San Jose City", "Nueva Ecija"});
        provinceCityMap.put("Nueva Vizcaya", new String[]{"Bayombong", "Solano", "Villaverde", "Dupax del Norte", "Dupax del Sur"});
        provinceCityMap.put("Occidental Mindoro", new String[]{"Mamburao", "Sablayan", "Paluan", "Lubang", "Rizal"});
        provinceCityMap.put("Oriental Mindoro", new String[]{"Calapan", "Puerto Galera", "Baco", "Bansud", "Naujan"});
        provinceCityMap.put("Palawan", new String[]{"Puerto Princesa", "El Nido", "Coron", "San Vicente", "Roxas"});
        provinceCityMap.put("Pampanga", new String[]{"San Fernando", "Angeles", "Mabalacat", "Apalit", "Bacolor"});
        provinceCityMap.put("Pangasinan", new String[]{"Lingayen", "Dagupan", "San Carlos", "Urdaneta", "Villasis"});
        provinceCityMap.put("Quezon", new String[]{"Lucena", "Tayabas", "Sariaya", "Candelaria", "Real"});
        provinceCityMap.put("Quirino", new String[]{"Cabarroguis", "Diffun", "Nagtipunan", "Maddela", "Saguday"});
        provinceCityMap.put("Rizal", new String[]{"Antipolo", "Binangonan", "Angono", "Taytay", "Rodriguez"});
        provinceCityMap.put("Romblon", new String[]{"Romblon", "San Jose", "Odiongan", "Alcantara", "Concepcion"});
        provinceCityMap.put("Samar", new String[]{"Catbalogan", "Calbayog", "Basey", "Gandara", "San Jorge"});
        provinceCityMap.put("Sarangani", new String[]{"Alabel", "Glan", "Malapatan", "Maasim", "Soron"});
        provinceCityMap.put("Siquijor", new String[]{"Siquijor", "Lazi", "Maria", "San Juan", "Enrique Villanueva"});
        provinceCityMap.put("Sorsogon", new String[]{"Sorsogon City", "Bulusan", "Gubat", "Irosin", "Matnog"});
        provinceCityMap.put("South Cotabato", new String[]{"Koronadal", "General Santos", "Tupi", "Surallah", "Lake Sebu"});
        provinceCityMap.put("Southern Leyte", new String[]{"Maasin", "Baybay", "Sogod", "Hinunangan", "San Juan"});
        provinceCityMap.put("Sultan Kudarat", new String[]{"Isulan", "Sultan Kudarat", "Tacurong", "Esperanza", "President Quirino"});
        provinceCityMap.put("Sulu", new String[]{"Jolo", "Maimbung", "Indanan", "Parang", "Talipao"});
        provinceCityMap.put("Surigao del Norte", new String[]{"Surigao City", "Bislig", "Sison", "Carrascal", "Dapa"});
        provinceCityMap.put("Surigao del Sur", new String[]{"Tandag", "Bislig", "Cagwait", "Carrascale", "Lianga"});
        provinceCityMap.put("Tarlac", new String[]{"Tarlac City", "Anao", "Concepcion", "Gerona", "La Paz"});
        provinceCityMap.put("Tawi-Tawi", new String[]{"Bongao", "Sapa-Sapa", "Sitangkai", "Sapa-Sapa", "Languyan"});
        provinceCityMap.put("Zambales", new String[]{"Iba", "Olongapo", "Castillejos", "San Antonio", "San Marcelino"});
        provinceCityMap.put("Zamboanga del Norte", new String[]{"Dipolog", "Dapitan", "Jose Dalman", "Sergio Osmeña Sr.", "Polanco"});
        provinceCityMap.put("Zamboanga del Sur", new String[]{"Zamboanga City", "Molave", "Pagadian", "Dimataling", "Aurora"});
        provinceCityMap.put("Zamboanga Sibugay", new String[]{"Iligan City", "Imelda", "Malangas", "Payao", "Tukuran"});
    }

    // Load cities based on selected province
    private void loadCitiesForProvince(String province) {
        String[] cities = provinceCityMap.get(province);
        if (cities != null) {
            ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, cities);
            cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            citySpinner.setAdapter(cityAdapter);
        }
    }
}
