package site.delivra.application.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import site.delivra.application.config.HereApiConfig;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class TrafficTileController {

    private final HereApiConfig hereApiConfig;
    private final RestTemplate hereRestTemplate;

    @GetMapping("/navigation/tiles/traffic/{z}/{x}/{y}")
    public void trafficFlowTile(
            @PathVariable int z,
            @PathVariable int x,
            @PathVariable int y,
            HttpServletResponse response) throws IOException {

        String url = String.format(
                "https://traffic.maps.ls.hereapi.com/maptile/2.1/flowtile/newest/normal.day/%d/%d/%d/256/png8?apiKey=%s",
                z, x, y, hereApiConfig.getApiKey());

        ResponseEntity<byte[]> tile = hereRestTemplate.getForEntity(url, byte[].class);

        response.setContentType("image/png");
        if (tile.getBody() != null) {
            response.setContentLength(tile.getBody().length);
            response.getOutputStream().write(tile.getBody());
            response.getOutputStream().flush();
        }
    }
}
