package eu.daiad.web.controller.api;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.arduino.ArduinoIntervalQuery;
import eu.daiad.web.model.arduino.ArduinoMeasurement;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.repository.application.IArduinoDataRepository;

@RestController("RestArduinoDataController")
public class ArduinoDataController extends BaseController {

	private static final Log logger = LogFactory.getLog(ArduinoDataController.class);

	@Autowired
	private IArduinoDataRepository arduinoDataRepository;

	@RequestMapping(value = "/api/v1/arduino/store", method = RequestMethod.POST, consumes = "text/plain", produces = "text/plain")
	public ResponseEntity<String> storeData(@RequestBody String data) {

		String message = "";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);

		try {
			if (!StringUtils.isBlank(data)) {
				String[] items = StringUtils.split(data, ",");

				if ((items.length < 1) || (((items.length - 1) % 2) != 0)) {
					return new ResponseEntity<String>("Invalid number of arguments", headers, HttpStatus.BAD_REQUEST);
				}

				String deviceKey = items[0];
				ArrayList<ArduinoMeasurement> measurements = new ArrayList<ArduinoMeasurement>();

				for (int index = 1, count = items.length; index < count; index += 2) {
					ArduinoMeasurement measurement = new ArduinoMeasurement();
					measurement.setTimestamp(Long.parseLong(items[index]));
					measurement.setVolume(Long.parseLong(items[index + 1]));

					measurements.add(measurement);
				}

				this.arduinoDataRepository.storeData(deviceKey, measurements);
			}
			return new ResponseEntity<String>(message, headers, HttpStatus.OK);
		} catch (Exception ex) {
			message = ex.getMessage();

			logger.error("Failed to insert data from arduino device", ex);
		}

		return new ResponseEntity<String>(message, headers, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@RequestMapping(value = "/api/v1/arduino/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse loadData(@RequestBody ArduinoIntervalQuery query) {
		RestResponse response = new RestResponse();

		try {
			return this.arduinoDataRepository.searchData(query);
		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response.add(this.getError(ex));
		}

		return response;
	}
}