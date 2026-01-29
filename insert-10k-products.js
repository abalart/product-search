// Script para insertar 10,000 productos en MongoDB
const brands = ['Dell', 'HP', 'Lenovo', 'ASUS', 'Apple', 'MSI', 'Acer', 'Samsung', 'LG', 'Sony', 'Corsair', 'Razer', 'ROG', 'Alienware', 'ThinkPad'];
const categories = ['Electronics', 'Laptops', 'Gaming', 'Workstation', 'Ultrabook', 'Tablet', 'Monitor', 'Keyboard', 'Mouse', 'Headphones'];
const colors = ['Silver', 'Black', 'Gold', 'White', 'Blue', 'Red', 'Green', 'Purple'];
const sizes = ['13.3', '15.6', '17.3', '12', '14', '16'];
const tags = ['laptop', 'gaming', 'work', 'portable', 'lightweight', 'powerful', 'budget', 'premium', 'professional', 'student'];

let products = [];
for (let i = 0; i < 10000; i++) {
  const brand = brands[Math.floor(Math.random() * brands.length)];
  const category = categories[Math.floor(Math.random() * categories.length)];
  const basePrice = Math.floor(Math.random() * 3500) + 300;
  
  products.push({
    name: brand + ' Model ' + String(i).padStart(5, '0'),
    description: 'High-quality ' + category + ' with ' + brand + ' technology and performance optimization',
    sku: brand.substring(0, 3) + '-' + String(i).padStart(7, '0'),
    brand: brand,
    category: category,
    tags: [tags[Math.floor(Math.random() * tags.length)], tags[Math.floor(Math.random() * tags.length)]],
    price: basePrice,
    currency: 'USD',
    stock: Math.floor(Math.random() * 500),
    active: Math.random() > 0.1,
    rating: parseFloat((Math.random() * 2 + 3).toFixed(1)),
    reviewCount: Math.floor(Math.random() * 1000),
    imageUrls: ['https://example.com/product-' + i + '.jpg'],
    attributes: {
      color: colors[Math.floor(Math.random() * colors.length)],
      size: sizes[Math.floor(Math.random() * sizes.length)] + ' inch',
      weight: parseFloat((Math.random() * 3 + 1).toFixed(2)),
      weightUnit: 'kg'
    },
    createdAt: new Date(),
    updatedAt: new Date()
  });
}

// Insertar en lotes de 1000
for (let i = 0; i < products.length; i += 1000) {
  db.products.insertMany(products.slice(i, i + 1000));
  print('Insertados ' + (i + 1000) + ' productos...');
}

const total = db.products.countDocuments();
print('\n✅ INSERCIÓN COMPLETADA');
print('Total de productos: ' + total);
