package bearmaps.proj2c.server.handler.impl;

import bearmaps.proj2c.AugmentedStreetMapGraph;
import bearmaps.proj2c.server.handler.APIRouteHandler;
import org.junit.runner.Request;
import spark.Request;
import spark.Response;
import bearmaps.proj2c.utils.Constants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bearmaps.proj2c.utils.Constants.*;

/**
 * Handles requests from the web browser for map images. These images
 * will be rastered into one large image to be displayed to the user.
 * @author rahul, Josh Hug, _________
 */
public class RasterAPIHandler extends APIRouteHandler<Map<String, Double>, Map<String, Object>> {

    /**
     * Each raster request to the server will have the following parameters
     * as keys in the params map accessible by,
     * i.e., params.get("ullat") inside RasterAPIHandler.processRequest(). <br>
     * ullat : upper left corner latitude, <br> ullon : upper left corner longitude, <br>
     * lrlat : lower right corner latitude,<br> lrlon : lower right corner longitude <br>
     * w : user viewport window width in pixels,<br> h : user viewport height in pixels.
     **/
    private static final String[] REQUIRED_RASTER_REQUEST_PARAMS = {"ullat", "ullon", "lrlat",
            "lrlon", "w", "h"};

    /**
     * The result of rastering must be a map containing all of the
     * fields listed in the comments for RasterAPIHandler.processRequest.
     **/
    private static final String[] REQUIRED_RASTER_RESULT_PARAMS = {"render_grid", "raster_ul_lon",
            "raster_ul_lat", "raster_lr_lon", "raster_lr_lat", "depth", "query_success"};


    @Override
    protected Map<String, Double> parseRequestParams(Request request) {
        return getRequestParams(request, REQUIRED_RASTER_REQUEST_PARAMS);
    }

    /**
     * @param requestParams Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @param response : Not used by this function. You may ignore.
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image;
     *                    can also be interpreted as the length of the numbers in the image
     *                    string. <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */
    @Override
    public Map<String, Object> processRequest(Map<String, Double> requestParams, Response response) {
        Map<String, Object> results = new HashMap<>();
        double ullon = requestParams.get("ullon");
        double ullat = requestParams.get("ullat");
        double lrlon = requestParams.get("lrlon");
        double lrlat = requestParams.get("lrlat");

        double requestWidth = requestParams.get("w");
        double requestHeight = requestParams.get("h");

        results.put("query_success", validateRequest(ullon, ullat, lrlon, lrlat));

        double requestLonDDP = (lrlon - ullon) / requestWidth;
        int depth = requestDepth(requestLonDDP);

        if (ullon < ROOT_ULLON) ullon = ROOT_ULLON;
        if (ullat > ROOT_ULLAT) ullat = ROOT_ULLAT;
        if (lrlon > ROOT_LRLON) lrlon = ROOT_LRLON;
        if (lrlat < ROOT_LRLAT) lrlat = ROOT_LRLAT;

        // System.out.println("RequestLongDDP: " + requestLonDDP);
        System.out.println("depth: " + depth);
        // 用户请求的坐标应根据LonDDP转换成可视图像的最佳坐标（1. resultLonddp取小于requestLonddp中所有值的最大的值， 2. result 坐标必须完全覆盖request坐标）
        results.put("depth", depth);
        // 根据深度将地图划分格栅, N * N个格栅
        int N = (int)Math.pow(2, depth);
        System.out.println("col and row has " + N +"grids");
        double widthPerGrid = Math.abs(ROOT_LRLON - ROOT_ULLON) / N;
        // rasterULLon 为ullon所在格栅的起始UnitUllon， 其他同理

        double ulLonPos = Math.floor(Math.abs((ullon - ROOT_ULLON)) / widthPerGrid);
        System.out.println(ulLonPos);
        double resultUllon = ulLonPos * widthPerGrid + ROOT_ULLON;
        if (ullon <= ROOT_ULLON)
            resultUllon = ROOT_ULLON;
        double lrLonPos = Math.ceil(Math.abs((lrlon - ROOT_ULLON)) / widthPerGrid);
        System.out.println(lrLonPos);
        double resultLrlon = lrLonPos * widthPerGrid + ROOT_ULLON;
        if (lrlon >= ROOT_LRLON)
            resultLrlon = ROOT_LRLON;

        double heightPerGrid = Math.abs(ROOT_ULLAT - ROOT_LRLAT) / N;
        double ulLatPos = Math.floor(Math.abs(ROOT_ULLAT - ullat) / heightPerGrid);

        double resultUllat = ROOT_ULLAT - ulLatPos * heightPerGrid;
        if (ullat >= ROOT_ULLAT - heightPerGrid)
            resultUllat = ROOT_ULLAT;

        double lrLatPos = Math.ceil(Math.abs((lrlat - ROOT_ULLAT)) / heightPerGrid);

        double resultLrlat = ROOT_ULLAT - lrLatPos * heightPerGrid;
        if (lrlat <= ROOT_LRLAT)
            resultLrlat = ROOT_LRLAT;

        results.put("raster_ul_lon", resultUllon);
        results.put("raster_ul_lat", resultUllat);
        results.put("raster_lr_lon", resultLrlon);
        results.put("raster_lr_lat", resultLrlat);
        // Last step, put the string[][] of images

        // 4^D images totally
        int w = (int)(lrLonPos - ulLonPos);
        int h = (int)(lrLatPos - ulLatPos);
        System.out.println(w + "," + h + "-----> image index");
        String[][] images = new String[h][w];
        for (int i = 0 ; i < h; i++) {
            for (int j = 0; j < w; j++) {
                images[i][j] = String.format("d%s_x%s_y%s.png", depth, (int)ulLonPos+j, (int)ulLatPos+i);
            }
        }
        results.put("render_grid", images);
        System.out.println(results.get("query_success"));
        return results;
    }
    private boolean validateRequest(double ullon, double ullat, double lrlon, double lrlat) {
        // check the request params are valid
        if (ullon > ROOT_LRLON || ullat < ROOT_LRLAT || lrlon < ROOT_ULLON || lrlat > ROOT_ULLAT || ullon > lrlon || ullat < lrlat) {
            return false;
        }
        // 如果超出范围， 取存在的最大值

        return true;
    }
    private int requestDepth(double requestLonDDP) {
        int depth = 0;
        double lonDDP = (ROOT_LRLON - ROOT_ULLON) / TILE_SIZE;
        boolean changed = false;
        System.out.println(lonDDP);
        while (requestLonDDP < lonDDP) {
            if (depth == 7) break;
            depth++;
            lonDDP /= 2;

        }
        return depth;
    }

    @Override
    protected Object buildJsonResponse(Map<String, Object> result) {
        boolean rasterSuccess = validateRasteredImgParams(result);

        if (rasterSuccess) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            writeImagesToOutputStream(result, os);
            String encodedImage = Base64.getEncoder().encodeToString(os.toByteArray());
            result.put("b64_encoded_image_data", encodedImage);
        }
        return super.buildJsonResponse(result);
    }

    private Map<String, Object> queryFail() {
        Map<String, Object> results = new HashMap<>();
        results.put("render_grid", null);
        results.put("raster_ul_lon", 0);
        results.put("raster_ul_lat", 0);
        results.put("raster_lr_lon", 0);
        results.put("raster_lr_lat", 0);
        results.put("depth", 0);
        results.put("query_success", false);
        return results;
    }

    /**
     * Validates that Rasterer has returned a result that can be rendered.
     * @param rip : Parameters provided by the rasterer
     */
    private boolean validateRasteredImgParams(Map<String, Object> rip) {
        for (String p : REQUIRED_RASTER_RESULT_PARAMS) {
            if (!rip.containsKey(p)) {
                System.out.println("Your rastering result is missing the " + p + " field.");
                return false;
            }
        }
        if (rip.containsKey("query_success")) {
            boolean success = (boolean) rip.get("query_success");
            if (!success) {
                System.out.println("query_success was reported as a failure");
                return false;
            }
        }
        return true;
    }

    /**
     * Writes the images corresponding to rasteredImgParams to the output stream.
     * In Spring 2016, students had to do this on their own, but in 2017,
     * we made this into provided code since it was just a bit too low level.
     */
    private void writeImagesToOutputStream(Map<String, Object> rasteredImageParams,
                                                  ByteArrayOutputStream os) {
        String[][] renderGrid = (String[][]) rasteredImageParams.get("render_grid");
        int numVertTiles = renderGrid.length;
        int numHorizTiles = renderGrid[0].length;

        BufferedImage img = new BufferedImage(numHorizTiles * Constants.TILE_SIZE,
                numVertTiles * Constants.TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics graphic = img.getGraphics();
        int x = 0, y = 0;

        for (int r = 0; r < numVertTiles; r += 1) {
            for (int c = 0; c < numHorizTiles; c += 1) {
                graphic.drawImage(getImage(Constants.IMG_ROOT + renderGrid[r][c]), x, y, null);
                x += Constants.TILE_SIZE;
                if (x >= img.getWidth()) {
                    x = 0;
                    y += Constants.TILE_SIZE;
                }
            }
        }

        /* If there is a route, draw it. */
        double ullon = (double) rasteredImageParams.get("raster_ul_lon"); //tiles.get(0).ulp;
        double ullat = (double) rasteredImageParams.get("raster_ul_lat"); //tiles.get(0).ulp;
        double lrlon = (double) rasteredImageParams.get("raster_lr_lon"); //tiles.get(0).ulp;
        double lrlat = (double) rasteredImageParams.get("raster_lr_lat"); //tiles.get(0).ulp;

        final double wdpp = (lrlon - ullon) / img.getWidth();
        final double hdpp = (ullat - lrlat) / img.getHeight();
        AugmentedStreetMapGraph graph = SEMANTIC_STREET_GRAPH;
        List<Long> route = ROUTE_LIST;

        if (route != null && !route.isEmpty()) {
            Graphics2D g2d = (Graphics2D) graphic;
            g2d.setColor(Constants.ROUTE_STROKE_COLOR);
            g2d.setStroke(new BasicStroke(Constants.ROUTE_STROKE_WIDTH_PX,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            route.stream().reduce((v, w) -> {
                g2d.drawLine((int) ((graph.lon(v) - ullon) * (1 / wdpp)),
                        (int) ((ullat - graph.lat(v)) * (1 / hdpp)),
                        (int) ((graph.lon(w) - ullon) * (1 / wdpp)),
                        (int) ((ullat - graph.lat(w)) * (1 / hdpp)));
                return w;
            });
        }

        rasteredImageParams.put("raster_width", img.getWidth());
        rasteredImageParams.put("raster_height", img.getHeight());

        try {
            ImageIO.write(img, "png", os);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private BufferedImage getImage(String imgPath) {
        BufferedImage tileImg = null;
        if (tileImg == null) {
            try {
                // File in = new File(imgPath);
                // tileImg = ImageIO.read(in);
                tileImg = ImageIO.read(Thread.currentThread().getContextClassLoader().getResource(imgPath));
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return tileImg;
    }
}
