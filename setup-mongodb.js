// Setup MongoDB indices y datos de ejemplo
const db = client.getDB("product_search");

// Crear índices
db.products.createIndex({ name: "text", description: "text" }, { weights: { name: 10, description: 5 }, name: "text_search_index" });
db.products.createIndex({ category: 1, active: 1 }, { name: "category_active_idx" });
db.products.createIndex({ brand: 1, active: 1 }, { name: "brand_active_idx" });
db.products.createIndex({ price: 1, rating: -1 }, { name: "price_rating_idx" });
db.products.createIndex({ active: 1, stock: 1 }, { name: "active_stock_idx" });
db.products.createIndex({ tags: 1 }, { name: "tags_idx" });
db.products.createIndex({ sku: 1 }, { unique: true, name: "sku_unique_idx" });
db.products.createIndex({ rating: -1 }, { name: "rating_idx" });
db.products.createIndex({ createdAt: -1 }, { name: "created_at_idx" });

// Insertar datos de ejemplo
db.products.insertOne({
    name: "Laptop Dell XPS 15",
    description: "High-performance laptop with Intel i7 processor and NVIDIA graphics",
    sku: "DELL-XPS15-2024",
    brand: "Dell",
    category: "Electronics",
    tags: ["laptop", "gaming", "work"],
    price: 1299.99,
    currency: "USD",
    stock: 45,
    active: true,
    rating: 4.7,
    reviewCount: 328,
    imageUrls: ["https://example.com/dell-xps-1.jpg"],
    attributes: {
        color: "Silver",
        size: "15.6 inch",
        weight: 2.0,
        weightUnit: "kg"
    },
    createdAt: new Date(),
    updatedAt: new Date()
})

db.products.insertOne({
    name: "MacBook Pro 16",
    description: "Apple MacBook Pro with M3 chip, stunning display",
    sku: "APPLE-MBP16-2024",
    brand: "Apple",
    category: "Electronics",
    tags: ["laptop", "professional", "design"],
    price: 2499.99,
    currency: "USD",
    stock: 23,
    active: true,
    rating: 4.9,
    reviewCount: 512,
    imageUrls: ["https://example.com/macbook-1.jpg"],
    attributes: {
        color: "Space Gray",
        size: "16 inch",
        weight: 2.1,
        weightUnit: "kg"
    },
    createdAt: new Date(),
    updatedAt: new Date()
})

print("✅ MongoDB configurado correctamente")
