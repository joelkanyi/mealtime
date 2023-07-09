/*
 * Copyright 2022 Joel Kanyi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kanyideveloper.addmeal.presentation.addmeal

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.kanyidev.searchable_dropdown.SearchableExpandedDropDownMenu
import com.kanyideveloper.compose_ui.components.StandardToolbar
import com.kanyideveloper.core.util.compressImage
import com.kanyideveloper.core.util.createImageFile
import com.kanyideveloper.core.util.imageUriToImageBitmap
import com.kanyideveloper.core.util.isNumeric
import com.kanyideveloper.core.util.saveImage
import com.mr0xf00.easycrop.CropError
import com.mr0xf00.easycrop.CropResult
import com.mr0xf00.easycrop.crop
import com.mr0xf00.easycrop.rememberImageCropper
import com.mr0xf00.easycrop.ui.ImageCropperDialog
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun AddMealScreen(
    navigator: AddMealNavigator,
    viewModel: AddMealsViewModel = koinViewModel()
) {
    val mealName = viewModel.mealName.value
    val category = viewModel.category.value
    val complexity = viewModel.cookingComplexity.value
    val peopleServing = viewModel.peopleServing.value
    val cookingTime = viewModel.cookingTime.value

    val context = LocalContext.current
    val analyticsUtil = viewModel.analyticsUtil()

    val imageCropper = rememberImageCropper()
    val scope = rememberCoroutineScope()
    var imageUri by remember { mutableStateOf<File?>(null) }
    var compressedImageUri by remember { mutableStateOf<Uri?>(null) }

    var hasCamPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted ->
                hasCamPermission = granted
            }
        )

    val photoLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture(),
            onResult = { success ->
                if (success) {
                    if (imageUri != null) {
                        val bitmap = MediaStore.Images.Media.getBitmap(
                            context.contentResolver,
                            imageUri!!.toUri()
                        )
                        val compressedBitmap = compressImage(bitmap)
                        compressedImageUri = saveImage(context, compressedBitmap)

                        scope.launch {
                            when (imageCropper.crop(uri = imageUri!!.toUri(), context = context)) {
                                CropError.LoadingError -> {
                                    Toast.makeText(
                                        context,
                                        "CropError.LoadingError",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                                CropError.SavingError -> {
                                    Toast.makeText(
                                        context,
                                        "CropError.SavingError",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                                CropResult.Cancelled -> {
                                    Toast.makeText(
                                        context,
                                        "CropResult.Cancelled",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                                is CropResult.Success -> {
                                    viewModel.setMealImageUri(
                                        compressedImageUri
                                    )
                                }
                            }
                        }
                    }
                }
            }
        )

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                scope.launch {
                    when (val result = imageCropper.crop(uri = uri, context = context)) {
                        CropError.LoadingError -> {
                            Toast.makeText(context, "CropError.LoadingError", Toast.LENGTH_SHORT)
                                .show()
                        }
                        CropError.SavingError -> {
                            Toast.makeText(context, "CropError.SavingError", Toast.LENGTH_SHORT)
                                .show()
                        }
                        CropResult.Cancelled -> {
                            Toast.makeText(context, "CropResult.Cancelled", Toast.LENGTH_SHORT)
                                .show()
                        }
                        is CropResult.Success -> {
                            viewModel.setMealImageUri(
                                saveImage(
                                    context = context,
                                    bitmap = result.bitmap.asAndroidBitmap()
                                )
                            )
                        }
                    }
                }
            }
        }

    LaunchedEffect(key1 = true, block = {
        analyticsUtil.trackUserEvent("open add meal screen")
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    })

    val cropState = imageCropper.cropState
    if (cropState != null) ImageCropperDialog(state = cropState)

    Scaffold(
        topBar = {
            StandardToolbar(
                navigate = {
                    analyticsUtil.trackUserEvent("Go back from add meal screen")
                    navigator.popBackStack()
                },
                title = {
                    Text(text = "Add meal", fontSize = 18.sp)
                },
                showBackArrow = true,
                navActions = {
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .height(200.dp)
                        .clickable {
                            analyticsUtil.trackUserEvent("Add meal image clicked by camera")
                            val photoFile = createImageFile(context)

                            if (photoFile != null) {
                                val photoURI = FileProvider.getUriForFile(
                                    context,
                                    context.applicationContext.packageName + ".fileprovider",
                                    photoFile
                                )
                                imageUri = photoFile
                                photoLauncher.launch(photoURI)
                            }
                        }
                ) {
                    if (viewModel.mealImageUri.value == null) {
                        IconButton(onClick = {
                            val photoFile = createImageFile(context)

                            if (photoFile != null) {
                                val photoURI = FileProvider.getUriForFile(
                                    context,
                                    context.applicationContext.packageName + ".fileprovider",
                                    photoFile
                                )
                                imageUri = photoFile
                                photoLauncher.launch(photoURI)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Selected Image
                    viewModel.mealImageUri.value?.let { uri ->
                        Image(
                            modifier = Modifier
                                .fillMaxSize(),
                            bitmap = context.imageUriToImageBitmap(uri).asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .wrapContentHeight()
                        .wrapContentWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable(MutableInteractionSource(), null) {
                                analyticsUtil.trackUserEvent("Add meal image clicked by gallery")
                                galleryLauncher.launch("image/*")
                            },
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add image from gallery",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Meal Name",
                        style = MaterialTheme.typography.labelMedium
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = mealName.text,
                        onValueChange = {
                            viewModel.setMealNameState(value = it)
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(),
                        placeholder = {
                            Text(
                                text = "Meal Name",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Words
                        ),
                        isError = mealName.error != null
                    )
                    if (mealName.error != null) {
                        Text(
                            text = mealName.error ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(.5f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.labelMedium
                        )

                        SearchableExpandedDropDownMenu(
                            listOfItems = viewModel.categories,
                            modifier = Modifier.fillMaxWidth(),
                            onDropDownItemSelected = { item ->
                                viewModel.setCategory(item)
                            },
                            dropdownItem = { category ->
                                Text(text = category, color = Color.Black)
                            },
                            parentTextFieldCornerRadius = 4.dp,
                            isError = category.error != null,
                            colors = TextFieldDefaults.outlinedTextFieldColors()
                        )

                        if (category.error != null) {
                            Text(
                                text = category.error ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Cooking Complexity",
                            style = MaterialTheme.typography.labelMedium
                        )

                        SearchableExpandedDropDownMenu(
                            listOfItems = viewModel.cookingComplexities,
                            modifier = Modifier.fillMaxWidth(),
                            onDropDownItemSelected = { item ->
                                viewModel.setCookingComplexity(item)
                            },
                            dropdownItem = { complexity ->
                                Text(text = complexity, color = Color.Black)
                            },
                            parentTextFieldCornerRadius = 4.dp,
                            isError = complexity.error != null,
                            colors = TextFieldDefaults.outlinedTextFieldColors()
                        )

                        if (complexity.error != null) {
                            Text(
                                text = complexity.error ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(.5f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Cooking Time - ${cookingTime.text} Minutes",
                            style = MaterialTheme.typography.labelMedium
                        )

                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = cookingTime.text,
                            onValueChange = {
                                viewModel.setCookingTime(value = it)
                            },
                            colors = TextFieldDefaults.outlinedTextFieldColors(),
                            placeholder = {
                                Text(
                                    text = "Cooking Time",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number,
                            ),
                            isError = cookingTime.error != null
                        )

                        if (cookingTime.error != null) {
                            Text(
                                text = cookingTime.error ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Serving - ${peopleServing.text} People",
                            style = MaterialTheme.typography.labelMedium
                        )

                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = peopleServing.text,
                            onValueChange = {
                                viewModel.setPeopleServing(value = it)
                            },
                            colors = TextFieldDefaults.outlinedTextFieldColors(),
                            placeholder = {
                                Text(
                                    text = "People Serving",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number,
                            ),
                            isError = peopleServing.error != null
                        )

                        if (peopleServing.error != null) {
                            Text(
                                text = peopleServing.error ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (viewModel.mealImageUri.value == null) {
                            Toast.makeText(
                                context,
                                "Please select an image so as to proceed",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        } else if (mealName.text.isEmpty()) {
                            viewModel.setMealNameState(error = "Meal name cannot be empty")
                            return@Button
                        } else if (cookingTime.text.isEmpty()) {
                            viewModel.setCookingTime(error = "Cooking time cannot be empty")
                            return@Button
                        } else if (peopleServing.text.isEmpty()) {
                            viewModel.setPeopleServing(error = "People serving cannot be empty")
                            return@Button
                        } else if (category.text.isEmpty()) {
                            viewModel.setCategory(error = "Meal category cannot be empty")
                            return@Button
                        } else if (complexity.text.isEmpty()) {
                            viewModel.setCookingComplexity(
                                error = "Cooking complexity cannot be empty"
                            )
                            return@Button
                        }

                        if (!isNumeric(cookingTime.text)) {
                            Toast.makeText(
                                context,
                                "Cooking time contains non Integer Values",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (!isNumeric(peopleServing.text)) {
                            Toast.makeText(
                                context,
                                "People serving contains non Integer Values",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (cookingTime.text.toInt() <= 0) {
                            Toast.makeText(
                                context,
                                "Cooking time cannot be equal to 0",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (peopleServing.text.toInt() <= 0) {
                            Toast.makeText(
                                context,
                                "People serving cannot be equal to 0",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        navigator.openNextAddMealScreen(
                            imageUri = viewModel.mealImageUri.value!!,
                            mealName = viewModel.mealName.value.text,
                            cookingTime = viewModel.cookingTime.value.text.toInt(),
                            servingPeople = viewModel.peopleServing.value.text.toInt(),
                            complexity = complexity.text,
                            category = category.text
                        )
                        analyticsUtil.trackUserEvent("add_meal_next_button_clicked")
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        text = "Next",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
