package com.example.data.repository

import com.example.data.model.CustomizationGroup
import com.example.data.model.CustomizationOption
import com.example.data.model.GroceryCategory
import com.example.data.model.GroceryItem
import com.example.data.model.HeroArtwork
import com.example.data.model.MenuItem
import com.example.data.model.PromoCoupon
import com.example.data.model.Restaurant
import com.example.data.model.SourceType

object CatalogRepository {

  const val MART_VENDOR_ID = "crave_mart_express"
  const val MART_VENDOR_NAME = "CraveCart Quick Mart"

  val cuisineStories: List<String> = listOf(
    "All",
    "North Indian",
    "Woodfire Pizza",
    "Asian Bowls",
    "Burgers",
    "Biryani",
    "Desserts",
  )

  private val pizzaSizeGroup = CustomizationGroup(
    id = "size",
    title = "Choose Size",
    isRequired = true,
    maxSelection = 1,
    options = listOf(
      CustomizationOption("sz_reg", "Regular 9\" (Serves 1)", 0.0),
      CustomizationOption("sz_med", "Medium 11\" (Serves 2)", 4.50),
      CustomizationOption("sz_lg", "Artisan Feast 14\"", 8.00),
    ),
  )

  private val pizzaCrustGroup = CustomizationGroup(
    id = "crust",
    title = "Select Crust",
    isRequired = true,
    maxSelection = 1,
    options = listOf(
      CustomizationOption("cr_sour", "72h Sourdough Hand-Tossed", 0.0),
      CustomizationOption("cr_thin", "Roman Crispy Thin", 0.0),
      CustomizationOption("cr_cheese", "Mozzarella Stuffed Burst", 3.25),
    ),
  )

  private val pizzaToppingsGroup = CustomizationGroup(
    id = "toppings",
    title = "Extra Gourmet Toppings",
    isRequired = false,
    maxSelection = 3,
    options = listOf(
      CustomizationOption("tp_burrata", "Fresh Creamy Burrata", 3.50),
      CustomizationOption("tp_truffle", "Black Truffle Drizzle", 2.75),
      CustomizationOption("tp_jalapeno", "Pickled Jalapenos & Chili", 1.25),
      CustomizationOption("tp_mushroom", "Charred Wild Mushrooms", 2.00),
    ),
  )

  private val spiceLevelGroup = CustomizationGroup(
    id = "spice",
    title = "Spice Preference",
    isRequired = true,
    maxSelection = 1,
    options = listOf(
      CustomizationOption("sp_mild", "Mild & Aromatic", 0.0),
      CustomizationOption("sp_med", "Balanced Classic Heat", 0.0),
      CustomizationOption("sp_hot", "Fiery House Chili", 0.50),
    ),
  )

  private val bowlPortionGroup = CustomizationGroup(
    id = "portion",
    title = "Bowl Size & Base",
    isRequired = true,
    maxSelection = 1,
    options = listOf(
      CustomizationOption("bp_reg", "Classic Bowl (Steamed Rice)", 0.0),
      CustomizationOption("bp_quinoa", "Supergrain Quinoa Base", 2.00),
      CustomizationOption("bp_xl", "Hearty Double Protein Bowl", 4.50),
    ),
  )

  private val indianAddOnsGroup = CustomizationGroup(
    id = "addons_indian",
    title = "Pair With",
    isRequired = false,
    maxSelection = 3,
    options = listOf(
      CustomizationOption("ad_naan", "Garlic Butter Naan", 3.25),
      CustomizationOption("ad_raita", "Mint Cucumber Raita", 1.75),
      CustomizationOption("ad_gulab", "Warm Gulab Jamun (2 pcs)", 3.50),
    ),
  )

  private val burgerAddOnsGroup = CustomizationGroup(
    id = "addons_burger",
    title = "Upgrade Your Meal",
    isRequired = false,
    maxSelection = 3,
    options = listOf(
      CustomizationOption("bg_fries", "Truffle Parmesan Fries", 3.95),
      CustomizationOption("bg_cheese", "Double Aged Cheddar", 1.50),
      CustomizationOption("bg_shake", "Salted Caramel Shake", 4.50),
    ),
  )

  val restaurants: List<Restaurant> = listOf(
    Restaurant(
      id = "rest_saffron_hearth",
      name = "Saffron Hearth",
      cuisines = listOf("North Indian", "Biryani", "Kebabs"),
      area = "Indiranagar Central",
      rating = 4.7,
      reviewCount = "2.4k+",
      etaMinutes = 24,
      deliveryFee = 1.99,
      costForTwo = 28,
      isPureVeg = false,
      promoTag = "20% OFF above $18",
      artwork = HeroArtwork.FEATURED_BOWL,
      accentHex = 0xFFC81E32,
      tagline = "Slow-simmered curries & clay-oven biryani bowls",
    ),
    Restaurant(
      id = "rest_crimson_crust",
      name = "Crimson Crust Woodfire",
      cuisines = listOf("Woodfire Pizza", "Pasta", "Desserts"),
      area = "Maple Arts District",
      rating = 4.6,
      reviewCount = "1.8k+",
      etaMinutes = 28,
      deliveryFee = 0.0,
      costForTwo = 32,
      isPureVeg = true,
      promoTag = "FREE DELIVERY",
      artwork = HeroArtwork.FOOD_FEAST,
      accentHex = 0xFF9E1123,
      tagline = "72-hour fermented sourdough pies blistered at 900F",
    ),
    Restaurant(
      id = "rest_seoul_bowls",
      name = "Seoul & Broth Co.",
      cuisines = listOf("Asian Bowls", "Ramen", "Dumplings"),
      area = "SoHo Market Square",
      rating = 4.5,
      reviewCount = "950+",
      etaMinutes = 22,
      deliveryFee = 1.49,
      costForTwo = 26,
      isPureVeg = false,
      promoTag = "$4 OFF on orders $20+",
      artwork = HeroArtwork.FOOD_FEAST,
      accentHex = 0xFFB45309,
      tagline = "Hand-pulled noodles, crispy gyoza & gochujang bowls",
    ),
    Restaurant(
      id = "rest_emerald_leaf",
      name = "Emerald Leaf Kitchen",
      cuisines = listOf("North Indian", "Salads", "Healthy Bowls"),
      area = "Botanic Gardens",
      rating = 4.8,
      reviewCount = "3.1k+",
      etaMinutes = 19,
      deliveryFee = 0.99,
      costForTwo = 24,
      isPureVeg = true,
      promoTag = "TOP RATED • 19 MINS",
      artwork = HeroArtwork.FEATURED_BOWL,
      accentHex = 0xFF157A4B,
      tagline = "Wholesome farm-to-bowl thalis & protein-packed greens",
    ),
    Restaurant(
      id = "rest_smash_craft",
      name = "Brioche & Ember Smash",
      cuisines = listOf("Burgers", "Fries", "Shakes"),
      area = "Downtown Junction",
      rating = 4.4,
      reviewCount = "1.2k+",
      etaMinutes = 26,
      deliveryFee = 1.99,
      costForTwo = 22,
      isPureVeg = false,
      promoTag = "BUY 1 GET 1 FRIES",
      artwork = HeroArtwork.FOOD_FEAST,
      accentHex = 0xFFC2410C,
      tagline = "Crispy-edge smash burgers on toasted potato brioche",
    ),
    Restaurant(
      id = "rest_velvet_crumb",
      name = "Velvet Crumb Patisserie",
      cuisines = listOf("Desserts", "Bakery", "Coffee"),
      area = "Heritage Promenade",
      rating = 4.9,
      reviewCount = "1.6k+",
      etaMinutes = 18,
      deliveryFee = 0.0,
      costForTwo = 18,
      isPureVeg = true,
      promoTag = "BESTSELLER TREATS",
      artwork = HeroArtwork.FEATURED_BOWL,
      accentHex = 0xFF831843,
      tagline = "Basque cheesecakes, warm churros & single-origin brews",
    ),
  )

  val menuItems: List<MenuItem> = listOf(
    // Saffron Hearth
    MenuItem(
      id = "dish_royal_biryani",
      restaurantId = "rest_saffron_hearth",
      category = "Signature Bowls",
      name = "Royal Saffron Dum Biryani",
      description = "Aged basmati layered with charred tandoori skewers, caramelized shallots, mint, and cooling raita.",
      basePrice = 15.50,
      isVeg = false,
      isBestseller = true,
      rating = 4.8,
      customizationGroups = listOf(spiceLevelGroup, bowlPortionGroup, indianAddOnsGroup),
    ),
    MenuItem(
      id = "dish_paneer_tikka_bowl",
      restaurantId = "rest_saffron_hearth",
      category = "Signature Bowls",
      name = "Smoked Paneer Tikka Makhani Bowl",
      description = "Char-grilled cottage cheese cubes folded into velvety heirloom tomato butter gravy with jeera rice.",
      basePrice = 13.75,
      isVeg = true,
      isBestseller = true,
      rating = 4.7,
      customizationGroups = listOf(spiceLevelGroup, indianAddOnsGroup),
    ),
    MenuItem(
      id = "dish_garlic_naan_platter",
      restaurantId = "rest_saffron_hearth",
      category = "Tandoor Breads",
      name = "Truffle Garlic Naan & Dal Makhani",
      description = "24-hour black lentil stew finished with smoked butter and two blistered garlic flatbreads.",
      basePrice = 12.50,
      isVeg = true,
      rating = 4.6,
      customizationGroups = listOf(indianAddOnsGroup),
    ),

    // Crimson Crust Woodfire
    MenuItem(
      id = "dish_margherita_burrata",
      restaurantId = "rest_crimson_crust",
      category = "Woodfire Pizzas",
      name = "San Marzano Burrata Margherita",
      description = "Crushed Italian plum tomatoes, fior di latte, torn sweet basil, and cold-pressed olive oil.",
      basePrice = 14.95,
      isVeg = true,
      isBestseller = true,
      rating = 4.8,
      customizationGroups = listOf(pizzaSizeGroup, pizzaCrustGroup, pizzaToppingsGroup),
    ),
    MenuItem(
      id = "dish_wild_mushroom_truffle",
      restaurantId = "rest_crimson_crust",
      category = "Woodfire Pizzas",
      name = "Charred Shroom & Black Truffle Pie",
      description = "Roasted cremini and shiitake mushrooms, whipped ricotta, thyme, and roasted garlic cream.",
      basePrice = 16.50,
      isVeg = true,
      rating = 4.7,
      customizationGroups = listOf(pizzaSizeGroup, pizzaCrustGroup, pizzaToppingsGroup),
    ),
    MenuItem(
      id = "dish_tiramisu_jar",
      restaurantId = "rest_crimson_crust",
      category = "Sweet Finishes",
      name = "Espresso Mascarpone Tiramisu",
      description = "Savoiardi biscuits soaked in dark roast espresso layered with whipped mascarpone and cocoa.",
      basePrice = 7.25,
      isVeg = true,
      rating = 4.9,
      customizationGroups = emptyList(),
    ),

    // Seoul & Broth Co.
    MenuItem(
      id = "dish_spicy_miso_ramen",
      restaurantId = "rest_seoul_bowls",
      category = "Noodle & Broth",
      name = "Chili Sesame Miso Ramen",
      description = "18-hour savory broth, springy wheat noodles, jammy soy egg, charred corn, and crispy scallions.",
      basePrice = 14.25,
      isVeg = false,
      isBestseller = true,
      rating = 4.6,
      customizationGroups = listOf(spiceLevelGroup, bowlPortionGroup),
    ),
    MenuItem(
      id = "dish_crispy_tofu_bibimbap",
      restaurantId = "rest_seoul_bowls",
      category = "Rice Bowls",
      name = "Crispy Gochujang Tofu Bibimbap",
      description = "Glazed organic tofu, pickled shiitake, sesame spinach, kimchi slaw, and warm sushi rice.",
      basePrice = 13.00,
      isVeg = true,
      rating = 4.5,
      customizationGroups = listOf(spiceLevelGroup, bowlPortionGroup),
    ),

    // Emerald Leaf Kitchen
    MenuItem(
      id = "dish_harvest_protein_bowl",
      restaurantId = "rest_emerald_leaf",
      category = "Power Bowls",
      name = "Tandoori Chickpea & Avocado Bowl",
      description = "Spiced roasted chickpeas, Hass avocado, pomegranate, herb quinoa, and lemon tahini dressing.",
      basePrice = 12.95,
      isVeg = true,
      isBestseller = true,
      rating = 4.9,
      customizationGroups = listOf(bowlPortionGroup, indianAddOnsGroup),
    ),
    MenuItem(
      id = "dish_artisan_thali",
      restaurantId = "rest_emerald_leaf",
      category = "Wholesome Thalis",
      name = "Executive Homestyle Veg Thali",
      description = "Paneer curry, yellow tadka dal, seasonal sabzi, two multigrain rotis, pulao, and dessert.",
      basePrice = 14.50,
      isVeg = true,
      rating = 4.8,
      customizationGroups = listOf(spiceLevelGroup, indianAddOnsGroup),
    ),

    // Brioche & Ember Smash
    MenuItem(
      id = "dish_double_ember_smash",
      restaurantId = "rest_smash_craft",
      category = "Smash Burgers",
      name = "Double Crimson Ember Burger",
      description = "Two crispy seared patties, aged cheddar, caramelized onions, smoky house relish on toasted brioche.",
      basePrice = 11.95,
      isVeg = false,
      isBestseller = true,
      rating = 4.6,
      customizationGroups = listOf(spiceLevelGroup, burgerAddOnsGroup),
    ),
    MenuItem(
      id = "dish_crispy_halloumi_burger",
      restaurantId = "rest_smash_craft",
      category = "Smash Burgers",
      name = "Crispy Herb Brioche Veg Stack",
      description = "Golden panko-crusted vegetable & halloumi patty with pickled slaw and garlic aioli.",
      basePrice = 10.95,
      isVeg = true,
      rating = 4.4,
      customizationGroups = listOf(burgerAddOnsGroup),
    ),

    // Velvet Crumb Patisserie
    MenuItem(
      id = "dish_basque_cheesecake",
      restaurantId = "rest_velvet_crumb",
      category = "Signature Cakes",
      name = "Burnt Basque Vanilla Cheesecake",
      description = "Caramelized crust with a molten cream-cheese custard center and warm berry compote.",
      basePrice = 8.50,
      isVeg = true,
      isBestseller = true,
      rating = 4.9,
      customizationGroups = emptyList(),
    ),
    MenuItem(
      id = "dish_hazel_churros",
      restaurantId = "rest_velvet_crumb",
      category = "Warm Treats",
      name = "Cinnamon Churros & Dark Ganache",
      description = "Six crisp golden churros dusted in cardamom sugar with warm 70% dark chocolate dip.",
      basePrice = 7.75,
      isVeg = true,
      rating = 4.8,
      customizationGroups = emptyList(),
    ),
  )

  val groceryCategories: List<GroceryCategory> = listOf(
    GroceryCategory("cat_produce", "Fresh Produce", "Farm fruits & greens", "10 min", 0xFF157A4B),
    GroceryCategory("cat_dairy", "Dairy & Bread", "Milk, eggs, sourdough", "8 min", 0xFFD97706),
    GroceryCategory("cat_snacks", "Snacks & Drinks", "Chips, cold brews", "9 min", 0xFFC81E32),
    GroceryCategory("cat_pantry", "Pantry Staples", "Oils, rice, sauces", "12 min", 0xFF7C3AED),
    GroceryCategory("cat_bakery", "Bakery & Sweet", "Croissants & treats", "10 min", 0xFFBE185D),
    GroceryCategory("cat_frozen", "Ice Cream & Chill", "Gelato & frozen bites", "8 min", 0xFF0284C7),
  )

  val groceryItems: List<GroceryItem> = listOf(
    GroceryItem("mart_avocado", "cat_produce", "Organic Hass Avocados", "Pack of 2 (320g)", 3.99, 4.99, "20% OFF", isFlashDeal = true),
    GroceryItem("mart_strawberries", "cat_produce", "Sweet Driscoll Strawberries", "250g punnet", 4.49, 5.49, "18% OFF", isFlashDeal = true),
    GroceryItem("mart_baby_spinach", "cat_produce", "Triple-Washed Baby Spinach", "200g pack", 2.49, 2.99, null),
    GroceryItem("mart_cherry_tomatoes", "cat_produce", "Vine-Ripened Cherry Tomatoes", "250g pack", 2.79, 3.29, "15% OFF"),

    GroceryItem("mart_sourdough", "cat_dairy", "Artisan Country Sourdough Loaf", "450g fresh baked", 4.75, 5.50, "FRESH", isFlashDeal = true),
    GroceryItem("mart_milk", "cat_dairy", "Grass-Fed Whole Milk (Glass)", "1 Litre bottle", 3.49, 3.99, null),
    GroceryItem("mart_eggs", "cat_dairy", "Free-Range Pasture Eggs", "Pack of 12", 4.99, 5.79, "14% OFF", isVeg = false),
    GroceryItem("mart_greek_yogurt", "cat_dairy", "Thick Strained Greek Yogurt", "400g tub", 3.29, 3.79, null),

    GroceryItem("mart_kettle_chips", "cat_snacks", "Sea Salt & Cracked Pepper Chips", "150g bag", 2.99, 3.49, null, isFlashDeal = true),
    GroceryItem("mart_cold_brew", "cat_snacks", "Nitro Oat Latte Cold Brew", "250ml can", 3.25, 3.75, "13% OFF"),
    GroceryItem("mart_sparkling_water", "cat_snacks", "Blood Orange Sparkling Water", "Pack of 4", 4.50, 5.20, null),

    GroceryItem("mart_olive_oil", "cat_pantry", "Cold-Pressed Extra Virgin Olive Oil", "500ml bottle", 9.99, 12.50, "20% OFF"),
    GroceryItem("mart_basmati", "cat_pantry", "Aged Himalayan Basmati Rice", "1 kg pouch", 4.89, 5.99, "18% OFF"),
    GroceryItem("mart_chili_crisp", "cat_pantry", "Crunchy Garlic Chili Crisp", "210g jar", 5.49, 6.49, null),

    GroceryItem("mart_croissant", "cat_bakery", "French Butter Croissants", "Pack of 2", 4.25, 4.99, "15% OFF"),
    GroceryItem("mart_brownie", "cat_bakery", "Fudge Walnut Espresso Brownie", "160g box", 4.95, 5.50, null),

    GroceryItem("mart_gelato", "cat_frozen", "Salted Pistachio Artisan Gelato", "473ml pint", 6.49, 7.99, "19% OFF", isFlashDeal = true),
    GroceryItem("mart_sorbet", "cat_frozen", "Alphonso Mango Fruit Bars", "Pack of 4", 5.25, 6.00, null),
  )

  val coupons: List<PromoCoupon> = listOf(
    PromoCoupon(
      code = "CRAVE20",
      title = "20% OFF up to $6",
      subtitle = "Valid on Food & Mart orders $18+",
      minOrder = 18.0,
      discountAmount = 5.0,
      freeDelivery = false,
      applicableType = null,
    ),
    PromoCoupon(
      code = "FREEDEL",
      title = "Zero Delivery Fee",
      subtitle = "Free doorstep delivery on orders $12+",
      minOrder = 12.0,
      discountAmount = 0.0,
      freeDelivery = true,
      applicableType = null,
    ),
    PromoCoupon(
      code = "MART15",
      title = "$3.50 OFF Quick Mart",
      subtitle = "Instant savings on Mart baskets $15+",
      minOrder = 15.0,
      discountAmount = 3.5,
      freeDelivery = false,
      applicableType = SourceType.MART,
    ),
  )

  fun getMenuForRestaurant(restaurantId: String): List<MenuItem> =
    menuItems.filter { it.restaurantId == restaurantId }

  fun getRestaurantById(restaurantId: String): Restaurant? =
    restaurants.firstOrNull { it.id == restaurantId }
}
