/**
******
******
Loads google charts for the labeled properties
Author: Anna Primpeli, Petar Petrovski
******
******
**/

//Load libraries and set callbacks for charts
google.charts.load('43',  {packages: ['bar','line', 'corechart']});

google.charts.setOnLoadCallback(drawHeadphonesTablesLabeled);
google.charts.setOnLoadCallback(drawHeadphonesListsLabeled);
google.charts.setOnLoadCallback(drawHeadphonesTitleLabeled);
google.charts.setOnLoadCallback(drawHeadphonesDescLabeled);

google.charts.setOnLoadCallback(drawPhonesTablesLabeled);
google.charts.setOnLoadCallback(drawPhonesListsLabeled);
google.charts.setOnLoadCallback(drawPhonesTitleLabeled);
google.charts.setOnLoadCallback(drawPhoneDescLabeled);

google.charts.setOnLoadCallback(drawTVsTablesLabeled);
google.charts.setOnLoadCallback(drawTVsListsLabeled);
google.charts.setOnLoadCallback(drawTVsTitlesLabeled);
google.charts.setOnLoadCallback(drawTVsDescLabeled);

google.charts.setOnLoadCallback(drawBarHeadphonesPlds);
google.charts.setOnLoadCallback(drawBarTVsPlds);
google.charts.setOnLoadCallback(drawBarPhonesPlds);

google.charts.setOnLoadCallback(drawColumnTVsTables);
google.charts.setOnLoadCallback(drawColumnPhonesTables);
google.charts.setOnLoadCallback(drawColumnHeadphonesTables);

google.charts.setOnLoadCallback(drawColumnTVsLists);
google.charts.setOnLoadCallback(drawColumnPhonesLists);
google.charts.setOnLoadCallback(drawColumnHeadphonesLists);


/*
* All labeled properties for headphones
*/
function drawHeadphonesTitleLabeled() {

	var headphonesTitle = google.visualization.arrayToDataTable([
		['Properties in Title', '%Percentage Frequency'],
		['model', 17.33],
		['product_type', 15.79],
		['brand', 10.97],
		['condition', 8.94],
		['headphone_use', 5.66],
		['color', 5.52],
		['headphones_technology', 4.12],
		['headphones_form_factor', 4.12],
		['additional_features', 0.91],
		['headphones_cup_type', 0.70],
		['compatibility', 0.70],
		['connectivity_technology', 0.42],
		['cables_included', 0.42],
		['product_code', 0.28],
		['magnet_material', 0.21],
		['pick-up_pattern', 0.14],
		['bluetooth_type', 0.07],
		['origin_assembly', 0.07]
	]);

	var options = {
		width: 550 ,
		height: 400,
		title: 'HEADPHONES - Distribution of Properties in Titles',
		chartArea: {'width': '80%', 'height': '80%'},
		colors:['red'],
		hAxis:{'textPosition': 'none'},
		is3D: true,
		legend: {position: 'none'}
	};

	var material = new google.visualization.ColumnChart(document.getElementById('headphones_Title'));
	material.draw(headphonesTitle, options);
  }

function drawHeadphonesDescLabeled() {

	var headphonesDesc = google.visualization.arrayToDataTable([
		['Properties in Description', '%Percentage Frequency'],
		['model', 14.24],
		['product_type', 13.35],
		['brand', 9.50],
		['headphones_technology', 8.61],
		['jack_plug', 8.61],
		['additional_features', 6.53],
		['color', 5.93],
		['included_accessories', 5.34],
		['headphones_form_factor', 5.04],
		['cable_length', 3.26],
		['cables_included', 3.26],
		['warranty', 2.97],
		['compatibility', 2.97],
		['headphone_use', 2.67],
		['product_gtin', 2.37],
		['frequency_response', 2.37],
		['impedance', 1.19],
		['magnet_material', 0.59],
		['max_input_power', 0.59],
		['sound_pressure_level', 0.59]

	]);

	var options = {
	  width: 550 ,
	  height: 400,
	  title: 'HEADPHONES - Distribution of Properties in Descriptions',
	  chartArea: {'width': '80%', 'height': '80%'},
	  is3D: true,
	  hAxis:{'textPosition': 'none'},
	  colors:['red'],
	  legend: {position: 'none'}
	};

	var material = new google.visualization.ColumnChart(document.getElementById('headphones_Desc'));
	material.draw(headphonesDesc, options);
  }

function drawHeadphonesTablesLabeled() {

	var headphonesTables = google.visualization.arrayToDataTable([
		['Properties in Tables', '%Percentage Frequency'],
		['mpn', 13.99],
		['brand', 12.50],
		['condition', 11.16],
		['model', 10.12],
		['product_gtin', 9.08],
		['impedance', 5.06],
		['additional_features', 4.91],
		['headphones_form_factor', 4.61],
		['color', 4.46],
		['headphones_cup_type', 3.72],
		['sensitivity', 3.42],
		['compatibility', 2.53],
		['warranty', 2.23],
		['connectivity_technology', 1.64],
		['included_accessories', 1.34],
		['jack_plug', 1.34],
		['sound_pressure_level', 1.19],
		['headphone_use', 1.19],
		['product_code', 1.04],
		['height', 0.60],
		['width', 0.60],
		['weight', 0.45],
		['origin_assembly', 0.30],
		['cable_length', 0.30],
		['min_frequency', 0.30],
		['frequency_response', 0.30],
		['max_frequency', 0.30],
		['max_input_power', 0.30],
		['headphones_technology', 0.30],
		['units', 0.15],
		['origin_components', 0.15],
		['has_warranty', 0.15],
		['package_weight', 0.15],
		['dimensions', 0.15]
	]);

	var options = {
		width: 550 ,
		height: 400,
		title: 'HEADPHONES - Distribution of Properties in Tables',
		is3D: true,
		chartArea: {'width': '80%', 'height': '80%'},
		hAxis:{'textPosition': 'none'},
		colors: ['red'],
		legend: {position: 'none'}
	};

	var material = new google.visualization.ColumnChart(document.getElementById('headphones_Tables'));
	material.draw(headphonesTables, options);
  }

function drawHeadphonesListsLabeled() {

	var headphonesLists = google.visualization.arrayToDataTable([
		['Properties in Lists', '%Percentage Frequency'],
		['impedance', 10.87],
		['sensitivity', 9.32],
		['frequency_response', 8.39],
		['cable_length', 7.14],
		['package_weight', 6.21],
		['headphones_technology', 5.59],
		['weight', 5.28],
		['connectivity_technology', 4.66],
		['color', 4.04],
		['max_input_power', 3.73],
		['jack_plug', 2.80],
		['headphones_form_factor', 2.17],
		['product_type', 2.17],
		['contact_pressure', 1.86],
		['headphones_cup_type', 1.86],
		['thd', 1.86],
		['model', 1.55],
		['condition', 1.55],
		['mpn', 1.24],
		['is_wireless', 0.93],
		['headphone_use', 0.93],
		['with_package', 0.93],
		['audio_interface_type', 0.93],
		['additional_features', 0.93],
		['sound_pressure_level', 0.93],
		['with_microphone', 0.93],
		['pick-up_pattern', 0.93],
		['battery_type', 0.62],
		['brand', 0.62],
		['codecs', 0.62],
		['operating_time', 0.62],
		['bluetooth_type', 0.62],
		['attenuation', 0.62],
		['included_accessories', 0.62],
		['supported_profiles', 0.62],
		['charging_time', 0.62],
		['microphone_response', 0.62],
		['cables_included', 0.62],
		['detachable_cable', 0.31],
		['diaphragm', 0.31],
		['gender', 0.31],
		['units', 0.31],
		['product_code', 0.31],
		['microphone_audio_details', 0.31],
		['magnet_material', 0.31],
		['frequency_characteristic', 0.31],
		['sound_output_mode', 0.31],
		['contacts', 0.31],
		['dimensions', 0.31]

	]);

	var options = {
		width: 550 ,
		height: 400,
		title: 'HEADPHONES - Distribution of Properties in Lists',
		chartArea: {'width': '80%', 'height': '80%'},
		is3D: true,
		hAxis:{'textPosition': 'none'},
		colors: ['red'],
		legend: {position: 'none'}
	};

	var material = new google.visualization.ColumnChart(document.getElementById('headphones_Lists'));
	material.draw(headphonesLists, options);
  }

/*
* All labeled properties for phones
*/
function drawPhonesTitleLabeled() {

	var phonesTitle = google.visualization.arrayToDataTable([
		['Properties in Titles', '%Percentage Frequency'],
		['phone_type', 22.97],
		['condition', 13.32],
		['color', 11.80],
		['brand', 10.03],
		['memory', 9.43],
		['phone_carrier', 6.05],
		['product_type', 5.92],
		['display_size', 3.76],
		['network_technology', 2.50],
		['computer_operating_system', 1.82],
		['processor_type', 1.61],
		['network_generation', 1.61],
		['ram', 1.52],
		['rear_cam_resolution', 1.48],
		['modelnum', 0.97],
		['mpn', 0.85],
		['display_resolution', 0.68],
		['connectivity', 0.47],
		['contract', 0.38],
		['cellular', 0.38],
		['input_n_output', 0.34],
		['body_material', 0.25],
		['display_type', 0.25],
		['browser_type', 0.21],
		['display_technology', 0.17],
		['box', 0.17],
		['shipping', 0.17],
		['band_mode', 0.17],
		['product_code', 0.13],
		['front_cam_resolution', 0.13],
		['product_gtin', 0.08],
		['video_quality', 0.08],
		['chipset', 0.08],
		['camera_features', 0.04],
		['SIM_card_quantity', 0.04],
		['core_count', 0.04],
		['additional_features', 0.04],
		['manufacturer_origin', 0.04]
	]);

	var options = {
		width: 550 ,
		height: 400,
		title: 'MOBILE PHONES - Distribution of Properties in Titles',
		chartArea: {'width': '80%', 'height': '80%'},
		is3D: true,
		hAxis:{'textPosition': 'none'},
		colors: ['blue'],
		legend: {position: 'none'}
	};

	var material = new google.visualization.ColumnChart(document.getElementById('phones_Title'));
	material.draw(phonesTitle, options);
  }

function drawPhoneDescLabeled() {

	var phonesDesc = google.visualization.arrayToDataTable([
		['Properties in Descriptions', '%Percentage Frequency'],
		['phone_type', 17.71],
		['cellular', 7.39],
		['network_technology', 6.56],
		['dimensions', 5.09],
		['processor_type', 4.95],
		['brand', 4.04],
		['computer_operating_system', 4.04],
		['chipset', 4.04],
		['rear_cam_resolution', 3.91],
		['display_type', 3.35],
		['camera_features', 3.21],
		['condition', 2.72],
		['phone_carrier', 2.65],
		['display_size', 2.23],
		['video_quality', 2.16],
		['bundled_items', 2.16],
		['memory', 2.09],
		['display_resolution', 1.81],
		['weight', 1.74],
		['color', 1.60],
		['display_technology', 1.32],
		['battery_type', 1.32],
		['ram', 1.32],
		['product_type', 1.26],
		['network_speed', 1.12],
		['modelnum', 1.05],
		['wattage', 0.98],
		['front_cam_resolution', 0.91],
		['screen_to_body_ratio', 0.84],
		['card_slot', 0.77],
		['pixel_density', 0.70],
		['network_generation', 0.63],
		['messaging', 0.56],
		['display_protection', 0.49],
		['body_material', 0.49],
		['wlan', 0.49],
		['product_code', 0.42],
		['mpn', 0.28],
		['standby_time', 0.28],
		['talk_time', 0.28],
		['bluetooth_type', 0.21],
		['usb', 0.14],
		['browser_type', 0.14],
		['gps_type', 0.14],
		['camera_autofocus', 0.14],
		['software', 0.07],
		['display_color', 0.07],
		['edge', 0.07],
		['body_form', 0.07]
	]);

	var options = {
		width: 550 ,
		height: 400,
		title: 'MOBILE PHONES - Distribution of Properties in Descriptions',
		hAxis:{'textPosition': 'none'},
		chartArea: {'width': '80%', 'height': '80%'},
		is3D: true,
		colors: ['blue'],
		legend: {position: 'none'}
	};

	var material = new google.visualization.ColumnChart(document.getElementById('phones_Desc'));
	material.draw(phonesDesc, options);
  }

function drawPhonesTablesLabeled() {

	var phonesTables = google.visualization.arrayToDataTable([
		['Properties in Tables', '%Percentage Frequency'],
		['phone_type', 5.75],
		['memory', 5.09],
		['brand', 4.94],
		['phone_carrier', 4.94],
		['display_size', 4.43],
		['rear_cam_resolution', 4.34],
		['color', 4.29],
		['mpn', 4.05],
		['condition', 3.87],
		['computer_operating_system', 2.94],
		['network_technology', 2.72],
		['design', 2.12],
		['product_gtin', 1.99],
		['digital_camera', 1.90],
		['weight', 1.84],
		['wattage', 1.81],
		['product_type', 1.77],
		['touch_screen', 1.75],
		['contract', 1.73],
		['network_generation', 1.73],
		['bluetooth', 1.73],
		['speakerphone', 1.66],
		['height', 1.57],
		['depth', 1.57],
		['width', 1.57],
		['display_technology', 1.57],
		['gps', 1.57],
		['email', 1.55],
		['browser', 1.44],
		['battery_type', 1.35],
		['display_resolution', 1.35],
		['processor_type', 1.11],
		['ram', 1.06],
		['card_slot', 1.02],
		['additional_features', 0.97],
		['band_mode', 0.84],
		['talk_time', 0.80],
		['tagline', 0.71],
		['standby_time', 0.71],
		['connectivity', 0.62],
		['bundled_items', 0.60],
		['launch_date', 0.58],
		['dimensions', 0.53],
		['cellular', 0.49],
		['video_quality', 0.42],
		['front_cam_resolution', 0.38],
		['SIM_type', 0.38],
		['wifi', 0.35],
		['chipset', 0.33],
		['sensors', 0.33],
		['wlan', 0.33],
		['charger_and_cable', 0.31],
		['usb', 0.27],
		['gpu', 0.24],
		['warranty', 0.22],
		['display_type', 0.22],
		['messaging', 0.20],
		['bluetooth_type', 0.20],
		['product_code', 0.20],
		['modelnum', 0.20],
		['QWERTY_physical_keyboard', 0.20],
		['gps_type', 0.20],
		['SIM_card_quantity', 0.18],
		['headphones_included', 0.18],
		['supported_audio_formats', 0.18],
		['nfc', 0.18],
		['display_color', 0.15],
		['touchscreen_type', 0.15],
		['camera_flash', 0.15],
		['audio_connectors', 0.15],
		['display_format', 0.15],
		['radio', 0.15],
		['auto_focus', 0.15],
		['front_camera', 0.13],
		['keypad_type', 0.13],
		['rear_camera', 0.13],
		['audio_time', 0.13],
		['mp3_player', 0.13],
		['charger_connection_type', 0.13],
		['manufacturer', 0.11],
		['wireless_charging', 0.11],
		['water_resistance', 0.09],
		['multitouch', 0.07],
		['3g_capable', 0.07],
		['alert_types', 0.07],
		['launch_status', 0.07],
		['4g_capable', 0.07],
		['3.5mm_jack', 0.07],
		['manufacturer_origin', 0.07],
		['box', 0.07],
		['global_ready', 0.04],
		['java', 0.04],
		['ports', 0.04],
		['no_bundled_items', 0.04],
		['browser_type', 0.04],
		['camera_features', 0.04],
		['display_protection', 0.04],
		['package_weight', 0.04],
		['software', 0.02],
		['network_speed', 0.02],
		['infrared_port', 0.02],
		['supported_picture_formats', 0.02],
		['body_material', 0.02],
		['user_manual', 0.02],
		['language', 0.02],
		['edge', 0.02],
		['camera_type', 0.02],
		['english_firmware', 0.02],
		['hearing_aid_compatible', 0.02],
		['gprs', 0.02]
	]);

	var options = {
		width: 550 ,
		height: 400,
		title: 'MOBILE PHONES - Distribution of Properties in Tables',
		hAxis:{'textPosition': 'none'},
		chartArea: {'width': '80%', 'height': '80%'},
		is3D: true,
		colors: ['blue'],
		legend: {position: 'none'}
	};

	var material = new google.visualization.ColumnChart(document.getElementById('phones_Tables'));
	material.draw(phonesTables, options);
  }

function drawPhonesListsLabeled() {

	var phonesLists = google.visualization.arrayToDataTable([
		['Properties in Lists', '%Percentage Frequency'],
		['memory', 4.60],
		['computer_operating_system', 4.26],
		['processor_type', 4.09],
		['display_type', 3.92],
		['dimensions', 3.41],
		['additional_features', 3.41],
		['battery_type', 3.24],
		['ram', 3.07],
		['rear_cam_resolution', 3.07],
		['display_size', 2.90],
		['video_quality', 2.90],
		['talk_time', 2.56],
		['phone_type', 2.39],
		['cellular', 2.39],
		['display_resolution', 2.39],
		['chipset', 2.39],
		['condition', 2.21],
		['language', 2.04],
		['camera_type', 1.70],
		['wattage', 1.70],
		['network_technology', 1.70],
		['brand', 1.53],
		['color', 1.53],
		['launch_date', 1.53],
		['SIM_card_quantity', 1.36],
		['manufacturer', 1.36],
		['depth', 1.36],
		['design', 1.36],
		['tagline', 1.19],
		['product_code', 1.19],
		['messaging', 1.02],
		['bundled_items', 1.02],
		['band_mode', 1.02],
		['warranty', 0.85],
		['supported_audio_formats', 0.85],
		['browser_type', 0.85],
		['weight', 0.85],
		['standby_time', 0.85],
		['wlan', 0.85],
		['mpn', 0.68],
		['bluetooth_type', 0.68],
		['product_gtin', 0.68],
		['camera_flash', 0.68],
		['card_slot', 0.68],
		['gps_type', 0.68],
		['sensors', 0.68],
		['store_options', 0.51],
		['alert_types', 0.51],
		['google_play', 0.51],
		['display_technology', 0.51],
		['modelnum', 0.51],
		['bluetooth', 0.51],
		['phone_carrier', 0.51],
		['connectivity', 0.34],
		['aspect_ratio', 0.34],
		['front_cam_resolution', 0.34],
		['usb', 0.34],
		['shipping', 0.34],
		['gps', 0.34],
		['gpu', 0.34],
		['input_n_output', 0.34],
		['display_protection', 0.34],
		['box', 0.34],
		['wireless_charging', 0.34],
		['product_type', 0.34],
		['camera_autofocus', 0.34],
		['package_weight', 0.34],
		['front_cam_aperture', 0.17],
		['multitouch', 0.17],
		['display_color', 0.17],
		['network_speed', 0.17],
		['java', 0.17],
		['rear_cam_aperture', 0.17],
		['launch_status', 0.17],
		['internet_time', 0.17],
		['contrast', 0.17],
		['infrared_port', 0.17],
		['digital_zoom', 0.17],
		['ports', 0.17],
		['touchscreen_type', 0.17],
		['speakerphone', 0.17],
		['system_requirements', 0.17],
		['audio_connectors', 0.17],
		['3.5mm_jack', 0.17],
		['services', 0.17],
		['network_generation', 0.17],
		['pixel_density', 0.17],
		['keypad_type', 0.17],
		['removable_battery', 0.17],
		['video_time', 0.17],
		['unlocked', 0.17],
		['radio', 0.17],
		['edge', 0.17],
		['camera_features', 0.17],
		['core_count', 0.17],
		['nfc', 0.17],
		['SIM_type', 0.17],
		['image_stabilization', 0.17],
		['audio_time', 0.17],
		['gprs', 0.17],
		['digital_camera', 0.17],
		['touch_screen', 0.17]
	]);

	var options = {
		width: 550 ,
		height: 400,
		title: 'MOBILE PHONES - Distribution of Properties in Lists',
		hAxis:{'textPosition': 'none'},
		chartArea: {'width': '80%', 'height': '80%'},
		is3D: true,
		colors: ['blue'],
		legend: {position: 'none'}
	};

	var material = new google.visualization.ColumnChart(document.getElementById('phones_Lists'));
	material.draw(phonesLists, options);
  }

/*
* All labeled properties for tvs
*/
function drawTVsTitlesLabeled() {

	var tvsTitles = google.visualization.arrayToDataTable([
		['Properties in Titles', '%Percentage Frequency'],
		['product_type', 20.22],
		['display_type', 16.92],
		['viewable_size', 11.83],
		['brand', 9.53],
		['total_size', 9.10],
		['model', 7.89],
		['video_signal_standard', 5.02],
		['display_resolution', 3.44],
		['computer_operating_system', 3.01],
		['refresh_rate', 2.58],
		['color', 1.79],
		['condition', 1.65],
		['smart_capable', 1.22],
		['sound_effects', 0.79],
		['series', 0.65],
		['scan_format', 0.50],
		['built_ins', 0.43],
		['manufacturer', 0.36],
		['3d', 0.36],
		['hdmi_ports', 0.36],
		['image_aspect_ratio', 0.36],
		['year_of_manufacture', 0.29],
		['connectivity', 0.29],
		['processor_type', 0.22],
		['triple_xd_engine', 0.22],
		['backlight_technology', 0.22],
		['rms_output_power', 0.22],
		['3d_technology', 0.22],
		['usb_ports', 0.14],
		['hdmi', 0.14],
		['image_contrast', 0.07]
	]);

	var options = {
		width: 550 ,
		height: 400,
		title: 'TELEVISIONS - Distribution of Properties in Titles',
		hAxis:{'textPosition': 'none'},
		chartArea: {'width': '80%', 'height': '80%'},
		is3D: true,
		colors: ['green'],
		legend: {position: 'none'}
	};

	var material = new google.visualization.ColumnChart(document.getElementById('tvs_Titles'));
	material.draw(tvsTitles, options);
  }

function drawTVsDescLabeled() {

	var tvsDesc = google.visualization.arrayToDataTable([
		['Properties in Descriptions', '%Percentage Frequency'],
		['display_type', 15.98],
		['video_signal_standard', 14.79],
		['input_connectors', 14.00],
		['product_type', 9.47],
		['brand', 7.50],
		['built_ins', 5.33],
		['display_resolution', 4.34],
		['model', 3.35],
		['total_size', 3.35],
		['smart_capable', 3.16],
		['digital_audio_output', 2.37],
		['supported_memory_cards', 1.78],
		['ports', 1.58],
		['processor_type', 1.18],
		['computer_operating_system', 1.18],
		['series', 1.18],
		['image_aspect_ratio', 0.99],
		['curved', 0.79],
		['wall_mountable', 0.79],
		['3d', 0.79],
		['mounting_features', 0.79],
		['remote_control_model', 0.79],
		['image_contrast', 0.59],
		['refresh_rate', 0.59],
		['headphone_jack', 0.39],
		['usb', 0.39],
		['product_code', 0.39],
		['sleep_wakeup_timer', 0.39],
		['scan_format', 0.39],
		['builtin_dvd_player', 0.39],
		['color', 0.20],
		['supported_audio_formats', 0.20],
		['digital_tv_tuner', 0.20],
		['v_chip_control', 0.20],
		['hdmi_ports', 0.20]
	]);

	var options = {
		width: 550 ,
		height: 400,
		title: 'TELEVISIONS - Distribution of Properties in Descriptions',
		hAxis:{'textPosition': 'none'},
		chartArea: {'width': '80%', 'height': '80%'},
		is3D: true,
		colors: ['green'],
		legend: {position: 'none'}
	};

	var material = new google.visualization.ColumnChart(document.getElementById('tvs_Desc'));
	material.draw(tvsDesc, options);
  }

function drawTVsTablesLabeled() {

	var tvsTables = google.visualization.arrayToDataTable([
		['Properties in Tables', '%Percentage Frequency'],
		['display_type', 7.10],
		['brand', 4.08],
		['viewable_size', 3.91],
		['warranty', 3.02],
		['model', 2.90],
		['display_resolution', 2.72],
		['mpn', 2.66],
		['product_code', 2.43],
		['weight', 2.37],
		['condition', 2.31],
		['color', 2.07],
		['video_inputs_qty', 1.89],
		['product_gtin', 1.83],
		['sound_effects', 1.66],
		['connectivity', 1.48],
		['hdmi_ports', 1.48],
		['product_type', 1.48],
		['refresh_rate', 1.48],
		['height', 1.42],
		['width', 1.42],
		['total_size', 1.42],
		['tagline', 1.36],
		['viewing_angle', 1.36],
		['speakers_qty', 1.36],
		['video_interface', 1.36],
		['depth_with_stand', 1.30],
		['depth', 1.30],
		['commercial_features', 1.30],
		['remote_control_model', 1.24],
		['usb_ports', 1.24],
		['tv_tuner', 1.18],
		['height_with_stand', 1.18],
		['speakers_type', 1.12],
		['image_aspect_ratio', 1.12],
		['additional_features', 1.07],
		['smart_capable', 1.07],
		['wall_mountable', 1.01],
		['weight_with_stand', 1.01],
		['digital_audio_format', 1.01],
		['3d_technology', 0.95],
		['media_card_slot', 0.95],
		['pc_input', 0.95],
		['rf_antenna_input', 0.89],
		['energy_star_certified', 0.83],
		['yearly_operating_cost', 0.77],
		['audio_surround', 0.77],
		['input_connectors', 0.77],
		['dimensions', 0.77],
		['audio_outputs', 0.77],
		['yearly_consumption', 0.77],
		['max_resolution', 0.77],
		['epaet_qualified', 0.77],
		['headphone_jack', 0.71],
		['ports', 0.71],
		['sleep_wakeup_timer', 0.71],
		['v_chip_control', 0.65],
		['series', 0.65],
		['rms_output_power', 0.65],
		['internet_services', 0.59],
		['3d', 0.59],
		['cables_included', 0.59],
		['bundled_items', 0.47],
		['curved', 0.47],
		['display_coating', 0.41],
		['image_contrast', 0.41],
		['voltage', 0.41],
		['brightness', 0.41],
		['power_supply', 0.41],
		['wall_mount_dimensions', 0.36],
		['origin', 0.36],
		['local_diming', 0.36],
		['scan_format', 0.30],
		['dimensions_without_stand', 0.30],
		['closed_caption_capability', 0.24],
		['input_video_formats', 0.24],
		['supported_memory_cards', 0.24],
		['wattage_operational', 0.24],
		['digital_audio_output', 0.24],
		['mounting_features', 0.24],
		['wattage_standby', 0.24],
		['remote_included', 0.24],
		['output_connectors', 0.18],
		['dlna', 0.18],
		['battery_type', 0.18],
		['builtin_dvd_player', 0.18],
		['multi_pack_indicator', 0.18],
		['supported_audio_formats', 0.18],
		['ethernet', 0.18],
		['package_weight', 0.18],
		['supported_languages', 0.12],
		['launch_year', 0.12],
		['component_in_ports', 0.12],
		['analog_tv_reception', 0.12],
		['sound_optimizer', 0.12],
		['builtin_tuner', 0.12],
		['dimensions_with_stand', 0.12],
		['backlight_technology', 0.12],
		['user_controls', 0.12],
		['digital_tv_tuner', 0.12],
		['picture_engine', 0.12],
		['rs232_ports', 0.12],
		['composite_in_ports', 0.12],
		['hd_capable', 0.12],
		['pixel_pitch', 0.12],
		['response_time', 0.12],
		['region_code', 0.12],
		['led', 0.06],
		['wifi', 0.06],
		['image_contrast_ratio', 0.06],
		['internet_streaming', 0.06],
		['usb', 0.06],
		['compliant_standards', 0.06],
		['ethernet_technology', 0.06],
		['pc_interface', 0.06],
		['channel_lock', 0.06],
		['built_ins', 0.06],
		['widescreen_modes', 0.06],
		['hdcp_compatability', 0.06]
	]);

	var options = {
		width: 550 ,
		height: 400,
		title: 'TELEVISIONS - Distribution of Properties in Tables',
		hAxis:{'textPosition': 'none'},
		chartArea: {'width': '80%', 'height': '80%'},
		is3D: true,
		colors: ['green'],
		legend: {position: 'none'}
	};

	var material = new google.visualization.ColumnChart(document.getElementById('tvs_Tables'));
	material.draw(tvsTables, options);
  }

function drawTVsListsLabeled() {

	var tvsLists = google.visualization.arrayToDataTable([
		['Properties in Lists', '%Percentage Frequency'],
		['viewable_size', 2.95],
		['3d_technology', 2.62],
		['speakers_type', 2.30],
		['usb', 2.30],
		['hdmi_ports', 2.30],
		['model', 1.97],
		['image_aspect_ratio', 1.97],
		['height', 1.64],
		['tagline', 1.64],
		['scan_format', 1.64],
		['depth', 1.64],
		['width', 1.64],
		['weight_with_stand', 1.64],
		['rms_output_power', 1.64],
		['usb_ports', 1.64],
		['hdmi', 1.64],
		['weight', 1.64],
		['additional_features', 1.64],
		['computer_operating_system', 1.64],
		['yearly_consumption', 1.64],
		['smart_capable', 1.64],
		['brightness', 1.64],
		['height_with_stand', 1.64],
		['sound_effects', 1.31],
		['wifi', 1.31],
		['streaming_service', 1.31],
		['sound_optimizer', 1.31],
		['depth_with_stand', 1.31],
		['width_with_stand', 1.31],
		['pc_streaming', 1.31],
		['video_inputs_qty', 1.31],
		['speakers', 1.31],
		['component_video', 1.31],
		['composite_video', 1.31],
		['product_name', 1.31],
		['ethernet', 1.31],
		['digital_audio_output', 1.31],
		['display_type', 1.31],
		['internet_access', 1.31],
		['wattage_standby', 1.31],
		['remote_included', 1.31],
		['product_type', 1.31],
		['warranty', 0.98],
		['3d_glasses_required', 0.98],
		['media_player', 0.98],
		['processor_type', 0.98],
		['display_resolution', 0.98],
		['ethernet_technology', 0.98],
		['interactive_features', 0.98],
		['max_resolution', 0.98],
		['refresh_rate', 0.98],
		['network_rj45', 0.98],
		['natural_voice_recognition', 0.66],
		['web_browser_included', 0.66],
		['tru_4k_engine', 0.66],
		['connectivity', 0.66],
		['screen_share', 0.66],
		['3d_glasses', 0.66],
		['tru_color_generator', 0.66],
		['ultra_hd', 0.66],
		['usb_version', 0.66],
		['wall_mount_dimensions', 0.66],
		['real_cinema_24p', 0.66],
		['4k_uhd_file_playback', 0.66],
		['motion_enhancement_technology', 0.66],
		['universal_control_capability', 0.66],
		['viewing_angle', 0.66],
		['speakers_qty', 0.66],
		['backlight_technology', 0.66],
		['triple_xd_engine', 0.66],
		['color', 0.66],
		['tru_4k_upscaler', 0.66],
		['wattage_operational', 0.66],
		['lg_content_store', 0.66],
		['green_compliant', 0.66],
		['total_size', 0.66],
		['yearly_operating_cost', 0.33],
		['wall_mountable', 0.33],
		['video_signal_standard', 0.33],
		['dlna', 0.33],
		['mpn', 0.33],
		['audio_surround', 0.33],
		['energy_star_certified', 0.33],
		['image_contrast_ratio', 0.33],
		['product_gtin', 0.33],
		['technical_specifications', 0.33],
		['remote_control_model', 0.33],
		['content_share', 0.33],
		['bundled_items', 0.33],
		['media_card_slot', 0.33],
		['builtin_tuner', 0.33],
		['rf_antenna_input', 0.33],
		['3d', 0.33],
		['digital_tv_tuner', 0.33],
		['video_interface', 0.33],
		['pc_input', 0.33],
		['epaet_qualified', 0.33]
	]);

	var options = {
		width: 550 ,
		height: 400,
		title: 'TELEVISIONS - Distribution of Properties in Lists',
		hAxis:{'textPosition': 'none'},
		chartArea: {'width': '80%', 'height': '80%'},
		is3D: true,
		colors: ['green'],
		legend: {position: 'none'}
	};

	var material = new google.visualization.ColumnChart(document.getElementById('tvs_Lists'));
	material.draw(tvsLists, options);
}

/*
* All labeled properties per pld per category
*/
function drawBarHeadphonesPlds() {

	var headphonesPld = google.visualization.arrayToDataTable([
		['Properties Used', 'Number of PLDs'],
		['Impedance', 3],
		['Sensitivity', 3],
		['Color', 3],
		['Weight', 3],
		['Contact pressure', 2],
		['Maximum Input Power',	2],
		['Fit Design', 2],
		['Cable Length', 2],
		['Frequency Response', 2]
	]);

	var options = {
		width: 550,
		height: 400,
		title: 'Headphones - Frequent usage of properties per PLD',
		chartArea: {'width': '50%', 'height': '80%'},
		colors:['red'],
		hAxis:{
			title: 'Number of PLDs',
			minValue: 0,
			maxValue: 5
		},
		is3D: true,
		legend: {position: 'none'}
	};

	var material = new google.visualization.BarChart(document.getElementById('headphones_pld'));
	material.draw(headphonesPld, options);
}

function drawBarPhonesPlds() {

	var phonesPld = google.visualization.arrayToDataTable([
		['Properties Used', 'Number of PLDs'],
		['Color', 8],
		['Camera', 6],
		['Weight', 6],
		['Internal Memory', 6],
		['Operating System', 6],
		['Battery Type', 5],
		['Condition', 5],
		['Processor', 5],
		['Battery', 5]

	]);

	var options = {
		width: 550,
		height: 400,
		title: 'Phones - Frequent usage of properties per PLD',
		chartArea: {'width': '50%', 'height': '80%'},
		colors:['blue'],
		hAxis:{
			title: 'Number of PLDs',
			minValue: 0,
			maxValue: 11
		},
		is3D: true,
		legend: {position: 'none'}
	};

	var material = new google.visualization.BarChart(document.getElementById('phones_pld'));
	material.draw(phonesPld, options);
}

function drawBarTVsPlds() {

	var tvsPld = google.visualization.arrayToDataTable([
		['Properties Used', 'Number of PLDs'],
		['Refresh Rate', 5],
		['Width', 5],
		['Screen Size', 4],
		['Brand' ,4],
		['UPC', 4],
		['Resolution', 4],
		['Aspect Ratio', 4],
		['Vertical Viewing Angle', 3],
		['Horizontal Viewing Angle', 3]
	]);

	var options = {
		width: 550,
		height: 400,
		title: 'TVs - Frequent usage of properties per PLD',
		chartArea: {'width': '50%', 'height': '80%'},
		colors:['green'],
		hAxis:{
			title: 'Number of PLDs',
			minValue: 0,
			maxValue: 8
		},
		is3D: true,
		legend: {position: 'none'}
	};

	var material = new google.visualization.BarChart(document.getElementById('tvs_pld'));
	material.draw(tvsPld, options);
}

/*
* Labeled properties per structural unit - table per category
*/
function drawColumnTVsTables() {

	var tvsTables = google.visualization.arrayToDataTable([
		['Properties Used', 'Number of Tables'],
		['brand', 83],
		['display_type', 67],
		['mpn', 66],
		['viewable_size', 58],
		['condition', 55],
		['display_resolution', 40],
		['model', 38],
		['product_type', 33],
		['weight', 33],
		['color',  33],
		['warranty', 30],
		['product_code', 17],
	]);

	var options = {
		width: 550,
		height: 400,
		title: 'TVs - Frequent usage of properties per Table',
		chartArea: {'width': '50%', 'height': '80%'},
		colors:['green'],
		vAxis:{
			title: 'Number of Tables',
			minValue: 0,
			maxValue: 111
		},
		hAxis:{
			title: 'Property',
			textPosition: 'none'
		},
		is3D: true,
		legend: {position: 'none'}
	};

	var material = new google.visualization.ColumnChart(document.getElementById('tvs_tables'));
	material.draw(tvsTables, options);
}

function drawColumnPhonesTables() {

	var phonesTables = google.visualization.arrayToDataTable([
		['Properties Used', 'Number of Tables'],
		['memory',  158],
		['brand', 156],
		['phone_type',  154],
		['color', 144],
		['display_size', 129],
		['mpn', 128],
		['rear_cam_resolution', 127],
		['phone_carrier', 126],
		['condition', 123],
		['computer_operating_system', 117]
	]);

	var options = {
		width: 550,
		height: 400,
		title: 'Phones - Frequent usage of properties per Table',
		chartArea: {'width': '65%', 'height': '80%'},
		colors:['blue'],
		vAxis:{
			title: 'Number of Tables',
			minValue: 0,
			maxValue: 181
		},
		hAxis:{
			title: 'Property',
			textPosition: 'none'
		},
		is3D: true,
		legend: {position: 'none'}
	};

	var material = new google.visualization.ColumnChart(document.getElementById('phones_tables'));
	material.draw(phonesTables, options);
}

function drawColumnHeadphonesTables() {

	var headphonesTables = google.visualization.arrayToDataTable([
		['Properties Used', 'Number of Tables'],
		['brand', 103],
		['condition', 102],
		['mpn', 91],
		['product_gtin', 53],
		['model', 46],
		['additional_features', 33],
		['color', 33],
		['impedance', 25],
		['headphones_cup_type', 22],
		['headphones_form_factor', 20]
	]);

	var options = {
		width: 550,
		height: 400,
		title: 'Headphones - Frequent usage of properties per Table',
		chartArea: {'width': '65%', 'height': '80%'},
		colors:['red'],
		vAxis:{
			title: 'Number of Lists',
			minValue: 0,
			maxValue: 112
		},
		hAxis:{
			title: 'Property',
			textPosition: 'none'
		},
		is3D: true,
		legend: {position: 'none'}
	};

	var material = new google.visualization.ColumnChart(document.getElementById('headphones_tables'));
	material.draw(headphonesTables, options);
}

/*
* Labeled properties per structural unit - list per category
*/
function drawColumnTVsLists() {

	var tvsLists = google.visualization.arrayToDataTable([
		['Properties Used', 'Number of Tables'],
		['viewable_size', 7],
		['hdmi_ports', 6],
		['3d_technology', 5],
		['computer_operating_system', 5],
		['usb', 5],
		['image_aspect_ratio', 5],
		['speakers_type', 3],
		['model', 3],
		['brightness', 3],
		['additional_features', 2]
	]);

	var options = {
		width: 550,
		height: 400,
		title: 'TVs - Frequent usage of properties per List',
		chartArea: {'width': '65%', 'height': '80%'},
		colors:['green'],
		vAxis:{
			title: 'Number of Lists',
			minValue: 0,
			maxValue: 29
		},
		hAxis:{
			title: 'Property',
			textPosition: 'none'
		},
		is3D: true,
		legend: {position: 'none'}
	};

	var material = new google.visualization.ColumnChart(document.getElementById('tvs_lists'));
	material.draw(tvsLists, options);
}

function drawColumnPhonesLists() {

	var phonesLists = google.visualization.arrayToDataTable([
		['Properties Used', 'Number of Lists'],
		['brand', 40],
		['product_type', 34],
		['modelnum', 34],
		['compatible_phones', 33],
		['function', 32],
		['retail_package', 32],
		['material', 30],
		['memory', 23],
		['package_weight', 23],
		['package_size', 22]
	]);

	var options = {
		width: 550,
		height: 400,
		title: 'Phones - Frequent usage of properties per List',
		chartArea: {'width': '65%', 'height': '80%'},
		colors:['blue'],
		vAxis:{
			title: 'Number of Lists',
			minValue: 0,
			maxValue: 75
		},
		hAxis:{
			title: 'Property',
			textPosition: 'none'
		},
		is3D: true,
		legend: {position: 'none'}
	};

	var material = new google.visualization.ColumnChart(document.getElementById('phones_lists'));
	material.draw(phonesLists, options);
}

function drawColumnHeadphonesLists() {

	var headphonesLists = google.visualization.arrayToDataTable([
		['Properties Used', 'Number of Lists'],
		['impedance', 25],
		['frequency_response', 24],
		['sensitivity', 23],
		['cable_length', 23],
		['package_weight', 20],
		['headphones_technology', 17],
		['weight', 17],
		['color', 14],
		['connectivity_technology', 12],
		['max_input_power', 12]
	]);

	var options = {
		width: 550,
		height: 400,
		title: 'Headphones - Frequent usage of properties per List',
		chartArea: {'width': '65%', 'height': '80%'},
		colors:['red'],
		vAxis:{
			title: 'Number of Lists',
			minValue: 0,
			maxValue: 69
		},
		hAxis:{
			title: 'Property',
			textPosition: 'none'
		},
		is3D: true,
		legend: {position: 'none'}
	};

	var material = new google.visualization.ColumnChart(document.getElementById('headphones_lists'));
	material.draw(headphonesLists, options);
}
