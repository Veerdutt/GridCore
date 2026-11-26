import warehouse.Item;
import warehouse.Position;
import warehouse.Warehouse;
import dispatch.OrderManager;
import dispatch.Picker;
import dispatch.PickerDispatcher;
import routing.Order;
import routing.RouteOptimizer;
import routing.RouteResult;
import pathfinding.PathFinder;
import pathfinding.PathResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Warehouse warehouse = new Warehouse();
        PathFinder pathFinder = new PathFinder(warehouse);
        RouteOptimizer routeOptimizer = new RouteOptimizer(pathFinder);

        List<Picker> pickers = new ArrayList<>();
        pickers.add(new Picker("Picker-1", warehouse.getPickerInitialPosition())); // (2,0)
        pickers.add(new Picker("Picker-2", new Position(5, 5)));
        pickers.add(new Picker("Picker-3", new Position(8, 2)));

        PickerDispatcher dispatcher = new PickerDispatcher(pickers, pathFinder);
        OrderManager orderManager = new OrderManager(dispatcher, routeOptimizer);

        runDemo(warehouse, pathFinder, pickers, orderManager);

        if (args.length > 0 && "--demo".equalsIgnoreCase(args[0])) {
            return;
        }

        runInteractiveMenu(warehouse, pathFinder, pickers, orderManager);
    }

    private static void runDemo(
        Warehouse warehouse,
        PathFinder pathFinder,
        List<Picker> pickers,
        OrderManager orderManager
    ) {
        System.out.println("==================================================================");
        System.out.println("   VIRTUAL WAREHOUSE & MULTI-AGENT PICKER SYSTEM");
        System.out.println("==================================================================");

        List<Position> pickerPositions = new ArrayList<>();
        for (Picker p : pickers) {
            pickerPositions.add(p.getCurrentPosition());
        }
        warehouse.printMapWithPickers(pickerPositions);

        System.out.println("\nInitial Pickers Status:");
        printPickersTable(pickers);

        // BFS shortest path demo
        Position bfsStart = new Position(2, 0);
        Position bfsEnd = new Position(2, 6);
        PathResult samplePath = pathFinder.findShortestPath(bfsStart, bfsEnd);
        System.out.println("\n==================================================================");
        System.out.println("BFS Shortest Path Demo: " + bfsStart + " -> " + bfsEnd);
        System.out.println("==================================================================");
        System.out.println("Reachable : " + samplePath.isReachable() + " | Distance: " + samplePath.getDistance() + " steps");
        System.out.println("Path      : " + formatPath(samplePath.getPath()));
        warehouse.printMapWithRoute(samplePath.getPath(), bfsStart, bfsEnd);

        // Edge case checks
        PathResult blockedCheck = pathFinder.findShortestPath(new Position(2, 0), new Position(1, 2));
        PathResult sameCheck = pathFinder.findShortestPath(new Position(2, 0), new Position(2, 0));
        System.out.println("\n--------------------- BFS EDGE CASE CHECKS -----------------------");
        System.out.println("  Blocked Destination (1,2) Reachable : " + blockedCheck.isReachable());
        System.out.println("  Same Start/Dest (2,0) Distance      : " + sameCheck.getDistance() + " steps");
        System.out.println("------------------------------------------------------------------");

        // Order 101 (5 items)
        Order order101 = new Order("101", List.of(
            warehouse.getItem("Milk"),
            warehouse.getItem("Bread"),
            warehouse.getItem("Apple"),
            warehouse.getItem("Rice"),
            warehouse.getItem("Juice")
        ));

        System.out.println("\n==================================================================");
        System.out.println("Processing Task #1: Order " + order101.getOrderId());
        System.out.println("==================================================================");
        if (orderManager.submitOrder(order101)) {
            printOrderRouteSummary(order101);
            printOrderMapRoute(warehouse, order101);
            printTimingReport(order101);

            System.out.println("\n>>> SIMULATING WAREHOUSE PICKING WORKFLOW:");
            orderManager.startPicking("101");
            List<Item> visitOrder = order101.getPlannedRoute().getVisitingOrder();
            for (int i = 0; i < visitOrder.size(); i++) {
                Item item = visitOrder.get(i);
                System.out.printf("    [Step %d/%d] Picker %s collected %s at %s.%n",
                    i + 1,
                    visitOrder.size(),
                    order101.getAssignedPicker().getPickerId(),
                    item.getName(),
                    item.getPosition()
                );
            }
            System.out.println("\n  [State Change] Order " + order101.getOrderId() + " -> " + order101.getStatus()
                + " | Picker " + order101.getAssignedPicker().getPickerId() + " -> " + order101.getAssignedPicker().getStatus());
        }

        // Order 102 concurrent while Picker-1 is PICKING
        Order order102 = new Order("102", List.of(
            warehouse.getItem("Apple"),
            warehouse.getItem("Juice")
        ));
        System.out.println("\n==================================================================");
        System.out.println("Processing Task #2 (Concurrent): Order " + order102.getOrderId());
        System.out.println("==================================================================");
        if (orderManager.submitOrder(order102)) {
            printOrderRouteSummary(order102);
            printTimingReport(order102);
            orderManager.startPicking("102");
            System.out.println("  [State Change] Order " + order102.getOrderId() + " -> " + order102.getStatus()
                + " | Picker " + order102.getAssignedPicker().getPickerId() + " -> " + order102.getAssignedPicker().getStatus());
        }

        // Complete Order 101 -> Picker becomes IDLE again
        System.out.println("\n==================================================================");
        System.out.println("Completing Order " + order101.getOrderId());
        System.out.println("==================================================================");
        Picker order101Picker = order101.getAssignedPicker();
        orderManager.startPacking("101");
        System.out.println("  [State Change] Picker " + order101Picker.getPickerId()
            + " status -> " + order101.getStatus() + " order " + order101.getOrderId() + ".");
        orderManager.completeOrder("101");
        System.out.println("  [State Change] Order " + order101.getOrderId() + " status -> " + order101.getStatus()
            + " | Total Completed Orders: " + orderManager.getCompletedOrders().size());
        System.out.println("  [State Change] Picker " + order101Picker.getPickerId()
            + " reset to " + order101Picker.getStatus()
            + " at " + order101Picker.getCurrentPosition() + ".");

        System.out.println("\nCurrent Pickers Status:");
        printPickersTable(pickers);
    }

    private static void runInteractiveMenu(
        Warehouse warehouse,
        PathFinder pathFinder,
        List<Picker> pickers,
        OrderManager orderManager
    ) {
        try (Scanner scanner = new Scanner(System.in)) {
            int nextOrderNum = 103;

            while (true) {
                System.out.println("\n==================================================================");
                System.out.println("   INTERACTIVE MENU");
                System.out.println("==================================================================");
                System.out.println("  1. View Warehouse Map");
                System.out.println("  2. View Pickers Status");
                System.out.println("  3. Find BFS Shortest Path");
                System.out.println("  4. Create & Dispatch New Order");
                System.out.println("  5. Advance Order State (Pick / Pack / Complete)");
                System.out.println("  0. Exit");
                System.out.println("------------------------------------------------------------------");
                System.out.print("Choose option: ");

                if (!scanner.hasNextLine()) {
                    break;
                }
                String choice = scanner.nextLine().trim();
                if ("0".equals(choice)) {
                    System.out.println("Exiting...");
                    break;
                }

                switch (choice) {
                    case "1":
                        warehouse.printMap();
                        break;
                    case "2":
                        printPickersTable(pickers);
                        break;
                    case "3":
                        try {
                            System.out.print("Enter start row col (e.g. 2 0): ");
                            if (!scanner.hasNextLine()) break;
                            String[] s = scanner.nextLine().trim().split("\\s+");
                            System.out.print("Enter dest row col (e.g. 9 6): ");
                            if (!scanner.hasNextLine()) break;
                            String[] d = scanner.nextLine().trim().split("\\s+");
                            Position start = new Position(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
                            Position dest = new Position(Integer.parseInt(d[0]), Integer.parseInt(d[1]));
                            PathResult res = pathFinder.findShortestPath(start, dest);
                            System.out.println("Reachable : " + res.isReachable() + " | Distance: " + res.getDistance() + " steps");
                            if (res.isReachable()) {
                                System.out.println("Path      : " + formatPath(res.getPath()));
                                warehouse.printMapWithRoute(res.getPath(), start, dest);
                            }
                        } catch (Exception e) {
                            System.out.println("Invalid coordinates.");
                        }
                        break;
                    case "4":
                        System.out.println("Available items: Milk, Bread, Apple, Rice, Juice");
                        System.out.print("Enter comma-separated item names: ");
                        if (!scanner.hasNextLine()) break;
                        String[] names = scanner.nextLine().split(",");
                        List<Item> orderItems = new ArrayList<>();
                        for (String name : names) {
                            Item item = warehouse.getItem(name.trim());
                            if (item != null) {
                                orderItems.add(item);
                            } else if (!name.trim().isEmpty()) {
                                System.out.println("Unknown item skipped: " + name.trim());
                            }
                        }
                        if (orderItems.isEmpty()) {
                            System.out.println("No valid items entered.");
                            break;
                        }
                        String orderId = String.valueOf(nextOrderNum);
                        Order newOrder = new Order(orderId, orderItems);
                        if (orderManager.submitOrder(newOrder)) {
                            nextOrderNum++;
                            printOrderRouteSummary(newOrder);
                            printOrderMapRoute(warehouse, newOrder);
                            printTimingReport(newOrder);
                        }
                        break;
                    case "5":
                        System.out.print("Enter Order ID: ");
                        if (!scanner.hasNextLine()) break;
                        String id = scanner.nextLine().trim();
                        System.out.print("Action (pick / pack / complete): ");
                        if (!scanner.hasNextLine()) break;
                        String action = scanner.nextLine().trim().toLowerCase();
                        if ("pick".equals(action)) {
                            orderManager.startPicking(id);
                        } else if ("pack".equals(action)) {
                            orderManager.startPacking(id);
                        } else if ("complete".equals(action)) {
                            orderManager.completeOrder(id);
                        } else {
                            System.out.println("Unknown action.");
                        }
                        printPickersTable(pickers);
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
            }
        }
    }

    private static void printPickersTable(List<Picker> pickers) {
        System.out.println("------------------------------------------------------------------");
        System.out.printf("%-10s | %-15s | %-12s | %-10s%n", "PICKER ID", "POSITION", "STATE", "ORDER ID");
        System.out.println("------------------------------------------------------------------");
        for (Picker p : pickers) {
            String orderId = p.getAssignedOrder() != null ? p.getAssignedOrder().getOrderId() : "None";
            System.out.printf("%-10s | %-15s | %-12s | %-10s%n",
                p.getPickerId(),
                p.getCurrentPosition(),
                p.getStatus(),
                orderId
            );
        }
        System.out.println("------------------------------------------------------------------");
    }

    private static String formatPath(List<Position> path) {
        if (path == null || path.isEmpty()) {
            return "(empty)";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) sb.append(" -> ");
            sb.append(path.get(i));
        }
        return sb.toString();
    }

    private static void printOrderRouteSummary(Order order) {
        RouteResult route = order.getPlannedRoute();
        List<RouteResult.Segment> segments = route.getSegments();
        System.out.println(">> Picker Assigned: " + order.getAssignedPicker().getPickerId()
            + " at position " + order.getAssignedPicker().getCurrentPosition());
        System.out.print(">> Visiting Order : ");
        for (int i = 0; i < route.getVisitingOrder().size(); i++) {
            if (i > 0) System.out.print(" -> ");
            System.out.print(route.getVisitingOrder().get(i).getName());
        }
        System.out.println();

        System.out.println("\n----------------- GREEDY MULTI-STOP ROUTE BREAKDOWN -----------------");
        for (int i = 0; i < segments.size(); i++) {
            RouteResult.Segment seg = segments.get(i);
            System.out.printf("  [Leg %d/%d] %s -> %s (%d %s)%n",
                i + 1,
                segments.size(),
                seg.fromLabel,
                seg.toLabel,
                seg.distance,
                seg.distance == 1 ? "step" : "steps"
            );
            System.out.println("             Path: " + formatPath(seg.path));
        }
        System.out.println("  Total Movement Steps across all " + route.getVisitingOrder().size()
            + " items: " + route.getTotalDistance() + " steps");
        System.out.println("--------------------------------------------------------------------");
    }

    private static void printOrderMapRoute(Warehouse warehouse, Order order) {
        Position startPos = order.getAssignedPicker().getCurrentPosition();
        List<Position> fullRoute = order.getPlannedRoute().getFullRoute();
        Position endPos = fullRoute.isEmpty() ? startPos : fullRoute.get(fullRoute.size() - 1);
        warehouse.printMapWithRoute(fullRoute, startPos, endPos);
    }

    private static void printTimingReport(Order order) {
        int steps = order.getPlannedRoute().getTotalDistance();
        int secondsPerStep = 3;
        int totalSeconds = steps * secondsPerStep;
        int minutes = totalSeconds / 60;
        int remSeconds = totalSeconds % 60;
        int slaTargetSeconds = 600;
        boolean withinSla = totalSeconds <= slaTargetSeconds;

        System.out.println("\n----------------- ROUTE EXECUTION & TIMING REPORT -----------------");
        System.out.println("Order ID               : " + order.getOrderId());
        System.out.println("Assigned Picker        : " + order.getAssignedPicker().getPickerId());
        System.out.println("Total Movement Steps   : " + steps + " steps");
        System.out.println("Travel Time Formula    : " + steps + " steps x " + secondsPerStep + "s/step");
        System.out.printf("Estimated Picking Time : %d seconds (%dm %02ds)%n", totalSeconds, minutes, remSeconds);
        System.out.println("10-Minute SLA Target   : " + slaTargetSeconds + " seconds (600s)");
        System.out.println("SLA Performance Check  : " + (withinSla ? "[WITHIN TARGET]" : "[EXCEEDS TARGET]"));
        System.out.println("--------------------------------------------------------------------");
    }
}
