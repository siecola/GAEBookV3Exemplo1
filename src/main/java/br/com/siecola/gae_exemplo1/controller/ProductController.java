package br.com.siecola.gae_exemplo1.controller;

import br.com.siecola.gae_exemplo1.model.Product;
import com.google.appengine.api.datastore.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import java.util.logging.Logger;

@RestController
@RequestMapping(path = "/api/products")
public class ProductController {
    private static final Logger log = Logger.getLogger("ProductController");

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Product>> getProducts() {
        List<Product> products = new ArrayList<>();
        DatastoreService datastore = DatastoreServiceFactory
                .getDatastoreService();

        Query query;
        query = new Query("Products").addSort("Code",
                Query.SortDirection.ASCENDING);

        List<Entity> productsEntities = datastore.prepare(query).asList(
                FetchOptions.Builder.withDefaults());

        for (Entity productEntity : productsEntities) {
            Product product = entityToProduct(productEntity);

            products.add(product);
        }

        return new ResponseEntity<List<Product>>(products, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/{code}")
    public ResponseEntity<Product> getProduct(@PathVariable int code) {
        DatastoreService datastore = DatastoreServiceFactory
                .getDatastoreService();

        Query.Filter codeFilter = new Query.FilterPredicate("Code",
                Query.FilterOperator.EQUAL, code);

        Query query = new Query("Products").setFilter(codeFilter);

        Entity productEntity = datastore.prepare(query).asSingleEntity();

        if (productEntity != null) {
            Product product = entityToProduct(productEntity);

            return new ResponseEntity<Product>(product, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Product> saveProduct(@RequestBody Product product) {

        DatastoreService datastore = DatastoreServiceFactory
                .getDatastoreService();

        if (!checkIfCodeExist (product)) {
            Key productKey = KeyFactory.createKey("Products", "productKey");
            Entity productEntity = new Entity("Products", productKey);

            productToEntity (product, productEntity);

            datastore.put(productEntity);

            product.setId(productEntity.getKey().getId());
        } else {
            return new ResponseEntity<Product>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Product>(product, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{code}")
    public ResponseEntity<Product> updateProduct(@RequestBody Product product,
                                                 @PathVariable("code") int code) {
        if (product.getId() != 0) {
            if (!checkIfCodeExist (product)) {
                DatastoreService datastore = DatastoreServiceFactory
                        .getDatastoreService();

                Query.Filter codeFilter = new Query.FilterPredicate("Code",
                        Query.FilterOperator.EQUAL, code);

                Query query = new Query("Products").setFilter(codeFilter);

                Entity productEntity = datastore.prepare(query).asSingleEntity();

                if (productEntity != null) {
                    productToEntity (product, productEntity);

                    datastore.put(productEntity);

                    product.setId(productEntity.getKey().getId());

                    return new ResponseEntity<Product>(product, HttpStatus.OK);
                } else {
                    return new ResponseEntity<Product>(HttpStatus.NOT_FOUND);
                }
            } else {
                return new ResponseEntity<Product>(HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<Product>(HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{code}")
    public ResponseEntity<Product> deleteProduct(@PathVariable("code") int code) {
        //Mensagem 1 - DEBUG
        log.fine("Tentando apagar produto com código=[" + code + "]");

        DatastoreService datastore = DatastoreServiceFactory
                .getDatastoreService();

        Query.Filter codeFilter = new Query.FilterPredicate("Code",
                Query.FilterOperator.EQUAL, code);

        Query query = new Query("Products").setFilter(codeFilter);

        Entity productEntity = datastore.prepare(query).asSingleEntity();

        if (productEntity != null) {
            datastore.delete(productEntity.getKey());

            //Mensagem 2 - INFO
            log.info("Produto com código=[" + code + "] " +
                    "apagado com sucesso");

            Product product = entityToProduct(productEntity);

            return new ResponseEntity<Product>(product, HttpStatus.OK);
        } else {
            //Mensagem 3 - ERROR
            log.severe ("Erro ao apagar produto com código=[" + code +
                    "]. Produto não encontrado!");

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private void productToEntity(Product product, Entity productEntity) {
        productEntity.setProperty("ProductID", product.getProductID());
        productEntity.setProperty("Name", product.getName());
        productEntity.setProperty("Code", product.getCode());
        productEntity.setProperty("Model", product.getModel());
        productEntity.setProperty("Price", product.getPrice());
    }

    public static Product entityToProduct(Entity productEntity) {
        Product product = new Product();
        product.setId(productEntity.getKey().getId());
        product.setProductID((String) productEntity.getProperty("ProductID"));
        product.setName((String) productEntity.getProperty("Name"));
        product.setCode(Integer.parseInt(productEntity.getProperty("Code")
                .toString()));
        product.setModel((String) productEntity.getProperty("Model"));
        product.setPrice(Float.parseFloat(productEntity.getProperty("Price")
                .toString()));
        return product;
    }

    private boolean checkIfCodeExist(Product product) {
        DatastoreService datastore = DatastoreServiceFactory
                .getDatastoreService();

        Query.Filter codeFilter = new Query.FilterPredicate("Code", Query.FilterOperator.EQUAL, product.getCode());

        Query query = new Query("Products").setFilter(codeFilter);
        Entity productEntity = datastore.prepare(query).asSingleEntity();

        if (productEntity == null) {
            return false;
        } else {
            if (productEntity.getKey().getId() == product.getId()) {
                //está alterando o mesmo produto
                return false;
            } else {
                return true;
            }
        }
    }
}